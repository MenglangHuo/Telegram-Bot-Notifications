package com.bronx.notification.service.impl;

import com.bronx.notification.dto.organizationUnit.OrganizationUnitRequest;
import com.bronx.notification.dto.organizationUnit.OrganizationUnitResponse;
import com.bronx.notification.dto.organizationUnit.OrganizationUnitTreeNode;
import com.bronx.notification.exceptions.BusinessException;
import com.bronx.notification.exceptions.ResourceNotFoundException;
import com.bronx.notification.mapper.OrganizationUnitMapper;
import com.bronx.notification.model.entity.Company;
import com.bronx.notification.model.entity.OrganizationUnit;
import com.bronx.notification.model.enumz.UnitStatus;
import com.bronx.notification.repository.CompanyRepository;
import com.bronx.notification.repository.OrganizationUnitRepository;
import com.bronx.notification.service.OrganizationUnitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrganizationUnitServiceImpl implements OrganizationUnitService {

    private final OrganizationUnitRepository orgUnitRepository;
    private final CompanyRepository companyRepository;
    private final OrganizationUnitMapper mapper;

    @Override
    @Transactional
    public OrganizationUnitResponse create(OrganizationUnitRequest request) {
        log.info("📝 Creating organization unit: {} ({})", request.getName(), request.getCode());

        // Validate company
        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        // Check for duplicate unit code
        if (orgUnitRepository.existsByCompanyIdAndUnitCode(
                request.getCompanyId(), request.getCode())) {
            throw new BusinessException("Unit code already exists in this company");
        }

        // Validate and set parent
        OrganizationUnit parent = null;
        if (request.getParentId() != null) {
            parent = orgUnitRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent unit not found"));

            // Validate parent belongs to same company
            if (!parent.getCompany().getId().equals(company.getId())) {
                throw new BusinessException("Parent unit must belong to the same company");
            }

            // Check if parent allows children
//            if (Boolean.TRUE.equals(parent.getIsLeaf())) {
//                throw new BusinessException("Parent unit is marked as leaf and cannot have children");
//            }
        }

        // Create entity
        OrganizationUnit orgUnit = mapper.toEntity(request);
        orgUnit.setCompany(company);
        orgUnit.setParent(parent);

        // Save to generate ID
        orgUnit = orgUnitRepository.save(orgUnit);

        // Compute path after ID is generated
        orgUnit.updatePath();
        orgUnit = orgUnitRepository.save(orgUnit);

        // Update parent's leaf status
        if (parent != null) {
            parent.setIsLeaf(false);
            orgUnitRepository.save(parent);
        }

        log.info("✅ Created organization unit: {} with path: {}",
                orgUnit.getName(), orgUnit.getPath());

        return mapper.toResponse(orgUnit);
    }

    @Override
    @Transactional
    public OrganizationUnitResponse update(Long id, OrganizationUnitRequest request) {
        log.info("📝 Updating organization unit: {}", id);

        OrganizationUnit orgUnit = findById(id);

        // Check if unit code changed and validate uniqueness
        if (request.getCode() != null &&
                !request.getCode().equals(orgUnit.getCode())) {
            if (orgUnitRepository.existsByCompanyIdAndUnitCode(
                    orgUnit.getCompany().getId(), request.getCode())) {
                throw new BusinessException("Unit code already exists in this company");
            }
        }

        // Update fields
        mapper.updateEntityFromRequest(request, orgUnit);

        OrganizationUnit updated = orgUnitRepository.save(orgUnit);

        log.info("✅ Updated organization unit: {}", updated.getName());

        return mapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationUnitResponse getById(Long id) {
        OrganizationUnit orgUnit = findById(id);
        OrganizationUnitResponse response = mapper.toResponse(orgUnit);

        // Add breadcrumbs
        response.setBreadcrumbs(buildBreadcrumbs(orgUnit));

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganizationUnitResponse> getByCompanyId(Long companyId) {
        List<OrganizationUnit> units = orgUnitRepository.findByCompanyId(companyId);
        return mapper.toResponseList(units);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("🗑️ Hard deleting organization unit: {}", id);

        if (!canDelete(id)) {
            throw new BusinessException("Cannot delete unit with active employees or subscriptions");
        }

        OrganizationUnit orgUnit = findById(id);

        // Check for children
        if (orgUnit.hasChildren()) {
            throw new BusinessException("Cannot delete unit with children. Delete children first.");
        }

        orgUnitRepository.delete(orgUnit);

        log.info("✅ Deleted organization unit: {}", id);
    }

    @Override
    @Transactional
    public void softDelete(Long id) {
        log.info("🗑️ Soft deleting organization unit: {}", id);

        OrganizationUnit orgUnit = findById(id);

        // Soft delete all descendants
        List<OrganizationUnit> descendants = orgUnitRepository
                .findDescendantsByPath(orgUnit.getPath());

        for (OrganizationUnit descendant : descendants) {
            descendant.setDeletedAt(Instant.now());
            descendant.setStatus(UnitStatus.INACTIVE);
        }

        orgUnit.setDeletedAt(Instant.now());
        orgUnit.setStatus(UnitStatus.INACTIVE);

        orgUnitRepository.save(orgUnit);
        orgUnitRepository.saveAll(descendants);

        log.info("✅ Soft deleted organization unit and {} descendants", descendants.size());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganizationUnitResponse> getRootUnits(Long companyId) {
        List<OrganizationUnit> roots = orgUnitRepository.findRootUnitsByCompanyId(companyId);
        return mapper.toResponseList(roots);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganizationUnitResponse> getChildren(Long parentId) {
        List<OrganizationUnit> children = orgUnitRepository.findChildrenByParentId(parentId);
        return mapper.toResponseList(children);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganizationUnitResponse> getDescendants(Long unitId) {
        OrganizationUnit unit = findById(unitId);
        List<OrganizationUnit> descendants = orgUnitRepository
                .findDescendantsByPath(unit.getPath());
        return mapper.toResponseList(descendants);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganizationUnitResponse> getAncestors(Long unitId) {
        OrganizationUnit unit = findById(unitId);
        List<Long> ancestorIds = unit.getAncestorIds();

        List<OrganizationUnit> ancestors = new ArrayList<>();
        for (Long ancestorId : ancestorIds) {
            orgUnitRepository.findById(ancestorId)
                    .ifPresent(ancestors::add);
        }

        return mapper.toResponseList(ancestors);
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationUnitTreeNode getTree(Long companyId) {
        List<OrganizationUnit> allUnits = orgUnitRepository
                .findByCompanyId(companyId);

        return buildTree(allUnits, null);
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationUnitTreeNode getSubtree(Long unitId) {
        OrganizationUnit root = findById(unitId);
        List<OrganizationUnit> descendants = orgUnitRepository
                .findDescendantsByPath(root.getPath());

        descendants.add(0, root); // Add root to the list

        return buildTree(descendants, unitId);
    }


    @Override
    @Transactional
    public void reorderChildren(Long parentId, List<Long> orderedChildIds) {
        log.info("🔢 Reordering children of parent: {}", parentId);

        List<OrganizationUnit> children = parentId != null ?
                orgUnitRepository.findChildrenByParentId(parentId) :
                orgUnitRepository.findRootUnitsByCompanyId(/* get from first child */null);

        for (int i = 0; i < orderedChildIds.size(); i++) {
            Long childId = orderedChildIds.get(i);
            OrganizationUnit child = children.stream()
                    .filter(c -> c.getId().equals(childId))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Child unit not found"));

            child.setDisplayOrder(i);
        }

        orgUnitRepository.saveAll(children);

        log.info("✅ Reordered {} children", orderedChildIds.size());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganizationUnitResponse> search(Long companyId, String searchTerm) {
        List<OrganizationUnit> results = orgUnitRepository
                .searchByCompanyId(companyId, searchTerm);
        return mapper.toResponseList(results);
    }

//    @Override
//    public List<OrganizationUnitResponse> getByType(Long companyId, UnitType unitType) {
//        return List.of();
//    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganizationUnitResponse> getByStatus(Long companyId, UnitStatus status) {
        List<OrganizationUnit> units = orgUnitRepository
                .findByCompanyAndStatus(companyId, status);
        return mapper.toResponseList(units);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganizationUnitResponse> getLeafNodes(Long companyId) {
        List<OrganizationUnit> leafNodes = orgUnitRepository.findLeafNodes(companyId);
        return mapper.toResponseList(leafNodes);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canDelete(Long unitId) {
//        // Check for active employees
//        Integer employeeCount = employeeRepository
//                .countActiveByOrganizationUnitId(unitId);
//        if (employeeCount > 0) {
//            return false;
//        }
//
//        // Check for active subscriptions
//        Optional<Subscription> activeSubscription = subscriptionRepository
//                .findActiveSubscriptionByOrgUnit(unitId);
//
//        return activeSubscription.isEmpty();
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isDescendantOf(Long childId, Long ancestorId) {
        OrganizationUnit child = findById(childId);
        List<Long> ancestors = child.getAncestorIds();
        return ancestors.contains(ancestorId);
    }

    @Override
    public void validateMove(Long unitId, Long newParentId) {
        if (unitId.equals(newParentId)) {
            throw new BusinessException("Unit cannot be its own parent");
        }

        if (newParentId != null && isDescendantOf(newParentId, unitId)) {
            throw new BusinessException("Cannot move unit to one of its descendants");
        }

        OrganizationUnit unit = findById(unitId);

        if (newParentId != null) {
            OrganizationUnit newParent = findById(newParentId);

            // Validate same company
            if (!unit.getCompany().getId().equals(newParent.getCompany().getId())) {
                throw new BusinessException("Cannot move unit to different company");
            }
        }
    }

    // Helper methods
    private OrganizationUnit findById(Long id) {
        return orgUnitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Organization unit not found with ID: " + id));
    }

    private void updateDescendantPaths(OrganizationUnit parentUnit) {
        List<OrganizationUnit> descendants = orgUnitRepository
                .findDescendantsByPath(parentUnit.getPath());

        for (OrganizationUnit descendant : descendants) {
            descendant.updatePath();
        }

        orgUnitRepository.saveAll(descendants);
    }

    private OrganizationUnitTreeNode buildTree(List<OrganizationUnit> units, Long rootId) {
        Map<Long, OrganizationUnitTreeNode> nodeMap = new HashMap<>();

        // Create all nodes
        for (OrganizationUnit unit : units) {
            OrganizationUnitTreeNode node = mapper.toTreeNode(unit);
            node.setChildren(new ArrayList<>());
            nodeMap.put(unit.getId(), node);
        }

        // Build tree structure
        OrganizationUnitTreeNode root = null;
        for (OrganizationUnit unit : units) {
            OrganizationUnitTreeNode node = nodeMap.get(unit.getId());

            if (unit.getParent() == null ||
                    (rootId != null && unit.getId().equals(rootId))) {
                root = node;
            } else if (unit.getParent() != null) {
                OrganizationUnitTreeNode parentNode = nodeMap.get(unit.getParent().getId());
                if (parentNode != null) {
                    parentNode.getChildren().add(node);
                }
            }
        }

        return root;
    }

    private List<String> buildBreadcrumbs(OrganizationUnit unit) {
        List<String> breadcrumbs = new ArrayList<>();
        OrganizationUnit current = unit;

        while (current != null) {
            breadcrumbs.add(0, current.getName());
            current = current.getParent();
        }

        breadcrumbs.add(0, unit.getCompany().getName());

        return breadcrumbs;
    }
}
