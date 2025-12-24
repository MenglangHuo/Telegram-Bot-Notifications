package com.bronx.telegram.notification.mapper;


import com.bronx.telegram.notification.dto.employee.EmployeeRequest;
import com.bronx.telegram.notification.dto.employee.EmployeeResponse;
import com.bronx.telegram.notification.model.entity.Employee;
import com.bronx.telegram.notification.repository.EmployeeRepository;
import com.bronx.telegram.notification.repository.TelegramBotRepository;
import com.bronx.telegram.notification.utils.EncryptionUtils;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {PartnerMapper.class,
                OrganizationUnitMapper.class,
                CompanyMapper.class
        },
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)

public interface EmployeeMapper {

    Employee toEntity(Employee employee);

    @Mapping(target = "telegramBotDeepLink", expression = "java(generateDeepLink(employee,repository))")
    EmployeeResponse toResponse(Employee employee,@Context TelegramBotRepository repository);

    void updateEmployee(EmployeeRequest request,@MappingTarget Employee employee);

    @Named("generateDeepLink")
    default String generateDeepLink(Employee employee, @Context TelegramBotRepository repository) {
        String botUsername = repository.findBotUsernameByEmployeeCode(employee.getEmployeeCode())
                .orElse(null);
        if (botUsername != null && !botUsername.isEmpty()) {
            String empCodeEncrypted= EncryptionUtils.encrypt(employee.getEmployeeCode());
            return String.format("https://t.me/%s?start=%s", botUsername, empCodeEncrypted);
       }
        return "https://t.me/" + botUsername;
    }
}
