package com.bronx.telegram.notification.service;
import com.bronx.telegram.notification.dto.organizationUnit.OrganizationUnitRequest;
import com.bronx.telegram.notification.dto.organizationUnit.OrganizationUnitResponse;
import com.bronx.telegram.notification.dto.organizationUnit.OrganizationUnitTreeNode;
import com.bronx.telegram.notification.model.entity.OrganizationUnit;
import com.bronx.telegram.notification.model.enumz.UnitStatus;
import com.bronx.telegram.notification.model.enumz.UnitType;

import java.util.List;

public interface OrganizationUnitService {

    // CRUD operations
    OrganizationUnitResponse create(OrganizationUnitRequest request);
    OrganizationUnitResponse update(Long id, OrganizationUnitRequest request);
    OrganizationUnitResponse getById(Long id);
    List<OrganizationUnitResponse> getByCompanyId(Long companyId);
    void delete(Long id);
    void softDelete(Long id);

    // Hierarchy operations
    List<OrganizationUnitResponse> getRootUnits(Long companyId);
    List<OrganizationUnitResponse> getChildren(Long parentId);
    List<OrganizationUnitResponse> getDescendants(Long unitId);
    List<OrganizationUnitResponse> getAncestors(Long unitId);
    OrganizationUnitTreeNode getTree(Long companyId);
    OrganizationUnitTreeNode getSubtree(Long unitId);

    // Move operations
//    void moveUnit(Long unitId, MoveUnitRequest request);
    void reorderChildren(Long parentId, List<Long> orderedChildIds);

    // Search and filter
    List<OrganizationUnitResponse> search(Long companyId, String searchTerm);
    List<OrganizationUnitResponse> getByType(Long companyId, UnitType unitType);
    List<OrganizationUnitResponse> getByStatus(Long companyId, UnitStatus status);
    List<OrganizationUnitResponse> getLeafNodes(Long companyId);

//    // Statistics
//    OrganizationUnitStats getStatistics(Long unitId);
//    Integer getTotalEmployeeCount(Long unitId);
//    void updateEmployeeCount(Long unitId);

    // Validation
    boolean canDelete(Long unitId);
    boolean isDescendantOf(Long childId, Long ancestorId);
    void validateMove(Long unitId, Long newParentId);
}

