package com.bronx.notification.controller;

import com.bronx.notification.dto.baseResponse.ApiResponse;
import com.bronx.notification.dto.organizationUnit.OrganizationUnitRequest;
import com.bronx.notification.dto.organizationUnit.OrganizationUnitResponse;
import com.bronx.notification.dto.organizationUnit.OrganizationUnitTreeNode;
import com.bronx.notification.model.enumz.UnitStatus;
import com.bronx.notification.service.OrganizationUnitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/organization-units")
@RequiredArgsConstructor
@Slf4j
public class OrganizationUnitController {

    private final OrganizationUnitService orgUnitService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ApiResponse<OrganizationUnitResponse>> create(
            @Valid @RequestBody OrganizationUnitRequest request) {
        OrganizationUnitResponse response = orgUnitService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Organization unit created successfully",response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<OrganizationUnitResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody OrganizationUnitRequest request) {

        log.info("📥 PUT /api/v1/organization-units/{} - Updating unit", id);
        OrganizationUnitResponse response = orgUnitService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Organization unit updated successfully",response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrganizationUnitResponse>> getById(@PathVariable Long id) {

        log.info("📥 GET /api/v1/organization-units/{}", id);
        OrganizationUnitResponse response = orgUnitService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<ApiResponse<List<OrganizationUnitResponse>>> getByCompany(
            @PathVariable Long companyId) {

        log.info("📥 GET /api/v1/organization-units/company/{}", companyId);

        List<OrganizationUnitResponse> response = orgUnitService.getByCompanyId(companyId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {

        log.info("📥 DELETE /api/v1/organization-units/{}", id);

        orgUnitService.delete(id);

        return ResponseEntity.ok(ApiResponse.success( "Organization unit deleted successfully",null));
    }

    // Hierarchy endpoints
    @GetMapping("/company/{companyId}/roots")
    public ResponseEntity<ApiResponse<List<OrganizationUnitResponse>>> getRootUnits(
            @PathVariable Long companyId) {

        log.info("📥 GET /api/v1/organization-units/company/{}/roots", companyId);

        List<OrganizationUnitResponse> response = orgUnitService.getRootUnits(companyId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}/children")
    public ResponseEntity<ApiResponse<List<OrganizationUnitResponse>>> getChildren(
            @PathVariable Long id) {

        log.info("📥 GET /api/v1/organization-units/{}/children", id);

        List<OrganizationUnitResponse> response = orgUnitService.getChildren(id);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}/descendants")
    public ResponseEntity<ApiResponse<List<OrganizationUnitResponse>>> getDescendants(
            @PathVariable Long id) {

        log.info("📥 GET /api/v1/organization-units/{}/descendants", id);

        List<OrganizationUnitResponse> response = orgUnitService.getDescendants(id);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}/ancestors")
    public ResponseEntity<ApiResponse<List<OrganizationUnitResponse>>> getAncestors(
            @PathVariable Long id) {

        log.info("📥 GET /api/v1/organization-units/{}/ancestors", id);

        List<OrganizationUnitResponse> response = orgUnitService.getAncestors(id);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/company/{companyId}/tree")
    public ResponseEntity<ApiResponse<OrganizationUnitTreeNode>> getTree(
            @PathVariable Long companyId) {

        log.info("📥 GET /api/v1/organization-units/company/{}/tree", companyId);

        OrganizationUnitTreeNode response = orgUnitService.getTree(companyId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}/subtree")
    public ResponseEntity<ApiResponse<OrganizationUnitTreeNode>> getSubtree(
            @PathVariable Long id) {

        log.info("📥 GET /api/v1/organization-units/{}/subtree", id);

        OrganizationUnitTreeNode response = orgUnitService.getSubtree(id);

        return ResponseEntity.ok(ApiResponse.success(response));
    }


    @PutMapping("/{parentId}/reorder")
    public ResponseEntity<ApiResponse<Void>> reorderChildren(
            @PathVariable Long parentId,
            @RequestBody List<Long> orderedChildIds) {

        log.info("📥 PUT /api/v1/organization-units/{}/reorder", parentId);

        orgUnitService.reorderChildren(parentId, orderedChildIds);

        return ResponseEntity.ok(ApiResponse.success( "Children reordered successfully",null));
    }

    // Search and filter
    @GetMapping("/company/{companyId}/search")
    public ResponseEntity<ApiResponse<List<OrganizationUnitResponse>>> search(
            @PathVariable Long companyId,
            @RequestParam String term) {

        log.info("📥 GET /api/v1/organization-units/company/{}/search?term={}", companyId, term);

        List<OrganizationUnitResponse> response = orgUnitService.search(companyId, term);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

//    @GetMapping("/company/{companyId}/type/{unitType}")
//    public ResponseEntity<ApiResponse<List<OrganizationUnitResponse>>> getByType(
//            @PathVariable Long companyId,
//            @PathVariable UnitType unitType) {
//
//        log.info("📥 GET /api/v1/organization-units/company/{}/type/{}", companyId, unitType);
//
//        List<OrganizationUnitResponse> response = orgUnitService.getByType(companyId, unitType);
//
//        return ResponseEntity.ok(ApiResponse.success(response));
//    }

    @GetMapping("/company/{companyId}/status/{status}")
    public ResponseEntity<ApiResponse<List<OrganizationUnitResponse>>> getByStatus(
            @PathVariable Long companyId,
            @PathVariable UnitStatus status) {

        log.info("📥 GET /api/v1/organization-units/company/{}/status/{}", companyId, status);

        List<OrganizationUnitResponse> response = orgUnitService.getByStatus(companyId, status);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/company/{companyId}/leaf-nodes")
    public ResponseEntity<ApiResponse<List<OrganizationUnitResponse>>> getLeafNodes(
            @PathVariable Long companyId) {

        log.info("📥 GET /api/v1/organization-units/company/{}/leaf-nodes", companyId);

        List<OrganizationUnitResponse> response = orgUnitService.getLeafNodes(companyId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

//    @GetMapping("/{id}/employee-count")
//    public ResponseEntity<ApiResponse<Map<String, Integer>>> getTotalEmployeeCount(
//            @PathVariable Long id) {
//
//        log.info("📥 GET /api/v1/organization-units/{}/employee-count", id);
//
//        Integer count = orgUnitService.getTotalEmployeeCount(id);
//
//        return ResponseEntity.ok(ApiResponse.success(Map.of("totalEmployees", count)));
//    }

//    @PostMapping("/{id}/update-employee-count")
//    public ResponseEntity<ApiResponse<Void>> updateEmployeeCount(@PathVariable Long id) {
//
//        log.info("📥 POST /api/v1/organization-units/{}/update-employee-count", id);
//
//        orgUnitService.updateEmployeeCount(id);
//
//        return ResponseEntity.ok(ApiResponse.success(, "Employee count updated",null));
//    }

    // Validation
    @GetMapping("/{id}/can-delete")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> canDelete(@PathVariable Long id) {

        log.info("📥 GET /api/v1/organization-units/{}/can-delete", id);

        boolean canDelete = orgUnitService.canDelete(id);

        return ResponseEntity.ok(ApiResponse.success(Map.of("canDelete", canDelete)));
    }

    @GetMapping("/{childId}/is-descendant-of/{ancestorId}")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> isDescendantOf(
            @PathVariable Long childId,
            @PathVariable Long ancestorId) {

        log.info("📥 GET /api/v1/organization-units/{}/is-descendant-of/{}", childId, ancestorId);

        boolean isDescendant = orgUnitService.isDescendantOf(childId, ancestorId);

        return ResponseEntity.ok(ApiResponse.success(Map.of("isDescendant", isDescendant)));
    }
}
