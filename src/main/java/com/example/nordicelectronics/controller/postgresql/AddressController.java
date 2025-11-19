package com.example.nordicelectronics.controller.postgresql;

import com.example.nordicelectronics.entity.Address;
import com.example.nordicelectronics.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name="PostgreSQL Address Controller", description="Handles operations related to addresses in PostgreSQL")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/postgresql/address")
public class AddressController {

    private final AddressService addressService;

    @Operation(summary="Get address by user email", description="Fetches an address by user email")
    @GetMapping("")
    public ResponseEntity<Address> get(@AuthenticationPrincipal UserDetails userDetails) {
        Address address = addressService.getByUserEmail(userDetails.getUsername());
        return ResponseEntity.ok(address);
    }

    @Operation(summary="Save address for user", description="Creates a new address for a user")
    @PostMapping("")
    public ResponseEntity<Address> save(@AuthenticationPrincipal UserDetails userDetails, 
                                       @RequestBody Address address) {
        Address savedAddress = addressService.saveForUser(userDetails.getUsername(), address);
        return new ResponseEntity<>(savedAddress, HttpStatus.CREATED);
    }

    @Operation(summary="Update address for user", description="Updates an existing address for a user")
    @PutMapping("")
    public ResponseEntity<Address> update(@AuthenticationPrincipal UserDetails userDetails,
                                          @RequestBody Address address) {
        Address updatedAddress = addressService.updateForUser(userDetails.getUsername(), address);
        return ResponseEntity.ok(updatedAddress);
    }

    @Operation(summary="Delete address for user", description="Deletes an address for a user")
    @DeleteMapping("")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserDetails userDetails) {
        addressService.deleteForUser(userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
