package com.shippex.util;

import com.shippex.dto.admin.AdminResponse;
import com.shippex.dto.admin.CreateAdminResponse;
import com.shippex.model.AdminUser;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AdminMapper {

    AdminResponse toResponse(AdminUser admin);

    CreateAdminResponse toCreateAdminResponse(AdminUser admin);

    List<AdminResponse> toResponseList(List<AdminUser> admins);

}
