package com.gearup.customerservice.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gearup.customerservice.domain.Customer;
import com.gearup.customerservice.domain.KycStatus;
import com.gearup.customerservice.service.CustomerService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService service;

    @GetMapping("/{firebaseUid}")
    public ResponseEntity<Customer> get(@PathVariable("firebaseUid") String firebaseUid) {
        Optional<Customer> c = service.get(firebaseUid);
        return c.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Customer> create(@RequestBody CreateRequest body) {
        Customer c = Customer.builder()
                .firebaseUid(body.getFirebaseUid())
                .email(body.getEmail())
                .displayName(body.getDisplayName())
                .phone(body.getPhone())
                .photoURL(body.getPhotoURL())
                .idNumber(body.getIdNumber())
                .address(body.getAddress())
                .birthday(body.getBirthday())
                .build();
        Customer created = service.create(c);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{firebaseUid}")
    public Customer update(@PathVariable("firebaseUid") String firebaseUid, @RequestBody UpdateRequest body) {
        Customer incoming = Customer.builder()
                .email(body.getEmail())
                .displayName(body.getDisplayName())
                .phone(body.getPhone())
                .photoURL(body.getPhotoURL())
                .idNumber(body.getIdNumber())
                .address(body.getAddress())
                .birthday(body.getBirthday())
                .build();
        return service.update(firebaseUid, incoming);
    }

    @PatchMapping("/{firebaseUid}/kyc")
    public Customer updateKyc(@PathVariable("firebaseUid") String firebaseUid, @RequestParam("status") String status) {
        KycStatus newStatus = KycStatus.valueOf(status);
        return service.updateKyc(firebaseUid, newStatus);
    }

    @Data
    public static class CreateRequest {
        @NotBlank
        private String firebaseUid;
        @NotBlank
        @Email
        private String email;
        private String displayName;
        private String phone;
        @JsonProperty("photoURL")
        private String photoURL;
        private String idNumber;
        private String address;
        private String birthday;
    }

    @Data
    public static class UpdateRequest {
        @NotBlank
        @Email
        private String email;
        private String displayName;
        private String phone;
        @JsonProperty("photoURL")
        private String photoURL;
        private String idNumber;
        private String address;
        private String birthday;
    }
}
