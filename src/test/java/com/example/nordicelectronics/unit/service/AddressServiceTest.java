package com.example.nordicelectronics.unit.service;

import com.example.nordicelectronics.entity.Address;
import com.example.nordicelectronics.entity.User;
import com.example.nordicelectronics.repositories.sql.AddressRepository;
import com.example.nordicelectronics.service.AddressService;
import com.example.nordicelectronics.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock
    AddressRepository addressRepository;

    @Mock
    UserService userService;

    @InjectMocks
    AddressService addressService;

    private Address makeAddress(UUID id) {
        Address adresse = new Address();
        adresse.setAddressId(id);
        adresse.setStreet("Main");
        adresse.setStreetNumber("1A");
        adresse.setCity("City");
        adresse.setZip("12345");
        return adresse;
    }

    private User makeUser(UUID userId, List<Address> addresses) {
        User user = new User();
        user.setUserId(userId);
        user.setEmail("user@example.com");
        user.setAddress(addresses);
        return user;
    }

    @Test
    void getById_found() {
        UUID id = UUID.randomUUID();
        Address newAddress = makeAddress(id); // create new address
        when(addressRepository.findById(id)).thenReturn(Optional.of(newAddress));

        Address getAddressById = addressService.getById(id);

        assertSame(newAddress, getAddressById);
        verify(addressRepository).findById(id);
    }

    @Test
    void getById_notFound_throws() {
        UUID id = UUID.randomUUID();
        when(addressRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> addressService.getById(id));
        verify(addressRepository).findById(id);
    }

    @Test
    void getByUserId_userHasNoAddress_throws() {
        UUID userId = UUID.randomUUID();
        User user = makeUser(userId, null);
        when(userService.findById(userId)).thenReturn(user);

        assertThrows(EntityNotFoundException.class, () -> addressService.getByUserId(userId));
        verify(userService).findById(userId);
    }

    @Test
    void getByUserId_userHasAddress_repositoryReturns() {
        UUID userId = UUID.randomUUID();
        UUID addrId = UUID.randomUUID();
        Address ref = makeAddress(addrId);
        // user has an Address object (with id) but service will still call repository
        Address userAddr = new Address();
        userAddr.setAddressId(addrId);
        User user = makeUser(userId, Collections.singletonList(userAddr));

        when(userService.findById(userId)).thenReturn(user);
        when(addressRepository.findById(addrId)).thenReturn(Optional.of(ref));

        Address foundAddressForUserById = addressService.getByUserId(userId);
        assertSame(ref, foundAddressForUserById);
        verify(userService).findById(userId);
        verify(addressRepository).findById(addrId);
    }

    @Test
    void getByUserEmail_delegatesToUser() {
        UUID userId = UUID.randomUUID();
        UUID addrId = UUID.randomUUID();
        Address ref = makeAddress(addrId);
        Address userAddr = new Address();
        userAddr.setAddressId(addrId);
        User user = makeUser(userId, Collections.singletonList(userAddr));

        when(userService.findByEmail("a@b.com")).thenReturn(user);
        when(userService.findById(userId)).thenReturn(user);
        when(addressRepository.findById(addrId)).thenReturn(Optional.of(ref));

        Address foundAddressForUserByEmail = addressService.getByUserEmail("a@b.com");
        assertSame(ref, foundAddressForUserByEmail);
        verify(userService).findByEmail("a@b.com");
        verify(addressRepository).findById(addrId);
    }

    @Test
    void save_delegatesToRepository() {
        Address address = makeAddress(UUID.randomUUID());
        when(addressRepository.save(address)).thenReturn(address);

        Address saved = addressService.save(address);
        assertSame(address, saved);
        verify(addressRepository).save(address);
    }

    @Test
    void saveForUser_whenUserAlreadyHasAddress_throws() {
        User existingUserWithAddress = makeUser(UUID.randomUUID(), Collections.singletonList(makeAddress(UUID.randomUUID())));
        when(userService.findByEmail("x@x.com")).thenReturn(existingUserWithAddress);

        assertThrows(IllegalStateException.class, () -> addressService.saveForUser("x@x.com", makeAddress(UUID.randomUUID())));
        verify(userService).findByEmail("x@x.com");
        verify(addressRepository, never()).save(any());
    }

    @Test
    void saveForUser_succeedsAndSavesUser() {
        UUID addrId = UUID.randomUUID();
        Address toSave = makeAddress(addrId);
        User userWithoutAddress = makeUser(UUID.randomUUID(), null);

        when(userService.findByEmail("ok@ok.com")).thenReturn(userWithoutAddress);
        when(addressRepository.save(toSave)).thenReturn(toSave);

        Address saved = addressService.saveForUser("ok@ok.com", toSave);

        assertSame(toSave, saved);
        // after save, userService.save should be called and user's address set
        verify(userService).findByEmail("ok@ok.com");
        verify(addressRepository).save(toSave);
        verify(userService).save(userWithoutAddress);
        assertNotNull(userWithoutAddress.getAddress());
        assertEquals(1, userWithoutAddress.getAddress().size());
        assertSame(toSave, userWithoutAddress.getAddress().get(0));
    }

    @Test
    void update_updatesFieldsAndSaves() {
        UUID id = UUID.randomUUID();
        Address existing = makeAddress(id);
        existing.setCity("OldCity");

        Address patch = new Address();
        patch.setStreet("NewStreet");
        patch.setStreetNumber("9B");
        patch.setCity("NewCity");
        patch.setZip("99999");

        when(addressRepository.findById(id)).thenReturn(Optional.of(existing));
        when(addressRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Address result = addressService.update(id, patch);

        assertEquals("NewStreet", result.getStreet());
        assertEquals("9B", result.getStreetNumber());
        assertEquals("NewCity", result.getCity());
        assertEquals("99999", result.getZip());
        verify(addressRepository).findById(id);
        verify(addressRepository).save(existing);
    }

    @Test
    void updateForUser_delegatesToUpdate() {
        UUID userId = UUID.randomUUID();
        UUID addrId = UUID.randomUUID();
        Address stored = makeAddress(addrId);
        User userWithStoredAddress = makeUser(userId, Collections.singletonList(stored));

        Address patch = new Address();
        patch.setCity("UpdatedCity");

        when(userService.findByEmail("who@who.com")).thenReturn(userWithStoredAddress);
        when(userService.findById(userId)).thenReturn(userWithStoredAddress);
        when(addressRepository.findById(addrId)).thenReturn(Optional.of(stored));
        when(addressRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Address res = addressService.updateForUser("who@who.com", patch);
        assertEquals("UpdatedCity", res.getCity());
        verify(userService).findByEmail("who@who.com");
        // AddressService.updateForUser triggers two repository lookups:
        // 1) getByUserId -> getById(addrId)
        // 2) update -> getById(addrId)
        verify(addressRepository, times(2)).findById(addrId);
        verify(addressRepository).save(stored);
    }

    @Test
    void deleteById_softDeletesAddress() {
        UUID id = UUID.randomUUID();
        Address address = makeAddress(id);
        when(addressRepository.findById(id)).thenReturn(Optional.of(address));
        when(addressRepository.save(any(Address.class))).thenReturn(address);

        addressService.deleteById(id);

        // soft delete should find entity, set deletedAt, and save
        verify(addressRepository).findById(id);
        verify(addressRepository).save(address);
        assertNotNull(address.getDeletedAt());
    }

    @Test
    void deleteById_throwsEntityNotFoundException_whenAddressNotFound() {
        UUID id = UUID.randomUUID();
        when(addressRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> addressService.deleteById(id));
        verify(addressRepository).findById(id);
        verify(addressRepository, never()).save(any());
    }

    @Test
    void deleteForUser_softDeletesAddress() {
        // setup
        UUID uniqueUserID = UUID.randomUUID();
        UUID uniqueAddressID = UUID.randomUUID();

        // newAddress associated with user
        Address newAddress = makeAddress(uniqueAddressID);

        // user with that address
        User user = makeUser(uniqueUserID, Collections.singletonList(newAddress));

        // find address from user object and assert its there
        when(userService.findByEmail("user@example.com")).thenReturn(user);
        when(userService.findById(uniqueUserID)).thenReturn(user);
        when(addressRepository.findById(uniqueAddressID)).thenReturn(Optional.of(newAddress));
        when(addressRepository.save(any(Address.class))).thenReturn(newAddress);

        // act
        addressService.deleteForUser("user@example.com");

        // verify - soft delete should save with deletedAt set
        verify(userService).findByEmail("user@example.com");
        verify(userService).findById(uniqueUserID);
        verify(addressRepository).findById(uniqueAddressID);
        verify(addressRepository).save(newAddress);
        assertNotNull(newAddress.getDeletedAt());
    }
}
