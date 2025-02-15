package ru.skillbox.social_network_post.controller.front_test;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.skillbox.social_network_post.dto.AccountDto;
import ru.skillbox.social_network_post.dto.AccountSearchDto;
import ru.skillbox.social_network_post.dto.Status;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1/account")
@RequiredArgsConstructor
public class AccountController {

//
//    @PutMapping("/recovery")
//    public ResponseEntity<Void> recoverAccount(@RequestBody AccountRecoveryRq recoveryRq) {
//        accountService.recoverAccount(recoveryRq);
//        return ResponseEntity.noContent().build();
//    }

    @GetMapping("/me")
    public ResponseEntity<AccountDto> getAccount() {
        //Account account = accountService.getAccount();
//        if (account == null) {
//            return ResponseEntity.notFound().build();
//        }

        AccountDto account = AccountDto.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .phone("+123456789")
                .photo("photo.jpg")
                .about("Test user")
                .city("Moscow")
                .country("Russia")
                .token("test-token")
                .statusCode(Status.FRIEND)
                .firstName("John")
                .lastName("Doe")
                .regDate(LocalDateTime.now())
                .birthDate(LocalDateTime.of(1990, 1, 1, 0, 0))
                .messagePermission("ALL")
                .lastOnlineTime(LocalDateTime.now().minusDays(1))
                .isOnline(false)
                .isBlocked(false)
                .isDeleted(false)
                .photoId(UUID.randomUUID().toString())
                .photoName("avatar.png")
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .password("securepassword")
                .build();

        return ResponseEntity.ok((account));
    }


//    @GetMapping
//    public ResponseEntity<List<AccountDto>> getAllAccounts(
//            @PageableDefault(page = 0, size = 1, sort = "id",
//                    direction = Sort.Direction.ASC) Pageable pageable) {
//        Page<Account> accounts = accountService.getAllAccounts(pageable);
//        return ResponseEntity.ok(accounts.stream().map(accountMapper::accountEntityToAccountDto).toList());
//    }



    @GetMapping("/{id}")
    public ResponseEntity<AccountDto> getAccountById(@PathVariable UUID id) {

        AccountDto account = AccountDto.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .phone("+123456789")
                .photo("photo.jpg")
                .about("Test user")
                .city("Moscow")
                .country("Russia")
                .token("test-token")
                .statusCode(Status.FRIEND)
                .firstName("John")
                .lastName("Doe")
                .regDate(LocalDateTime.now())
                .birthDate(LocalDateTime.of(1990, 1, 1, 0, 0))
                .messagePermission("ALL")
                .lastOnlineTime(LocalDateTime.now().minusDays(1))
                .isOnline(false)
                .isBlocked(false)
                .isDeleted(false)
                .photoId(UUID.randomUUID().toString())
                .photoName("avatar.png")
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .password("securepassword")
                .build();

        return ResponseEntity.ok((account));
    }

    @GetMapping("/search")
    public ResponseEntity<List<AccountDto>> searchAccount(
            @RequestParam(required = false) AccountSearchDto accountSearchDto) {

        AccountDto account = AccountDto.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .phone("+123456789")
                .photo("photo.jpg")
                .about("Test user")
                .city("Moscow")
                .country("Russia")
                .token("test-token")
                .statusCode(Status.FRIEND)
                .firstName("John")
                .lastName("Doe")
                .regDate(LocalDateTime.now())
                .birthDate(LocalDateTime.of(1990, 1, 1, 0, 0))
                .messagePermission("ALL")
                .lastOnlineTime(LocalDateTime.now().minusDays(1))
                .isOnline(false)
                .isBlocked(false)
                .isDeleted(false)
                .photoId(UUID.randomUUID().toString())
                .photoName("avatar.png")
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .password("securepassword")
                .build();
        return ResponseEntity.ok(Collections.singletonList(account));
    }

//    @GetMapping("/ids")
//    public ResponseEntity<String> getAllAccountsIds() {
//        String allIds = accountService.getAllAccountsIds();
//        return ResponseEntity.ok(allIds);
//    }

//    @GetMapping("/accountIds")
//    public ResponseEntity<List<AccountDto>> getAllAccountsByIds(
//            @RequestParam List<UUID> ids, @PageableDefault(page = 0, size = 1, sort = "id",
//            direction = Sort.Direction.ASC) @RequestParam Pageable pageable) {
//        //Page<Account> accounts = accountService.getAccountsByIds(ids, pageable);
//        return ResponseEntity.ok(accounts.stream().map(accountMapper::accountEntityToAccountDto).toList());
//    }
}
