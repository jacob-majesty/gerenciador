package com.gerenciador.projeto.client;

import com.gerenciador.projeto.dto.MemberDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "member-service", url = "${app.member-api.base-url}") // URL configurável no application.properties
public interface MemberApiClient {

    @GetMapping("/api/membros/{id}")
    MemberDTO getMemberById(@PathVariable("id") Long id);

}

