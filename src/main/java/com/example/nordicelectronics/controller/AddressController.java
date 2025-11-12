package com.example.nordicelectronics.controller;

import com.example.nordicelectronics.entity.Address;
import com.example.nordicelectronics.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/address")
public class AddressController {

    private final AddressService addressService;

    @GetMapping("")
    public ResponseEntity<Address> get(@AuthenticationPrincipal UserDetails userDetails) {
        Address address = addressService.getByUserEmail(userDetails.getUsername());
        return ResponseEntity.ok(address);
    }

    @PostMapping("")
    public ResponseEntity<Address> save(@AuthenticationPrincipal UserDetails userDetails, 
                                       @RequestBody Address address) {
        Address savedAddress = addressService.saveForUser(userDetails.getUsername(), address);
        return new ResponseEntity<>(savedAddress, HttpStatus.CREATED);
    }

    @PutMapping("")
    public ResponseEntity<Address> update(@AuthenticationPrincipal UserDetails userDetails,
                                          @RequestBody Address address) {
        Address updatedAddress = addressService.updateForUser(userDetails.getUsername(), address);
        return ResponseEntity.ok(updatedAddress);
    }

    @DeleteMapping("")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserDetails userDetails) {
        addressService.deleteForUser(userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
