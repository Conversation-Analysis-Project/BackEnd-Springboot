package com.sometimes.code.controller;


import com.sometimes.code.dto.myPage.*;
import com.sometimes.code.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/myPage")
public class UserController {

    private final UserService userService;
    @GetMapping("/userInfo")
    public ResponseEntity<UserInfoResponse> getUserInfo(@RequestHeader("Authorization") String token) {
        UserInfoResponse userInfo = userService.getUserInfo(token);
        return ResponseEntity.ok(userInfo);
    }

    @PostMapping("/changeNickName")
    public ResponseEntity<String> changeNickName(@RequestHeader("Authorization") String token,
                                                 @RequestBody ChangeNickNameRequest request) {
        userService.changeNickName(token, request.getNewNickName());
        return ResponseEntity.ok("Nickname updated successfully");
    }

    @PostMapping("/changeName")
    public ResponseEntity<String> changeName(@RequestHeader("Authorization") String token,
                                             @RequestBody ChangeNameRequest request) {
        userService.changeName(token, request.getNewName());
        return ResponseEntity.ok("Name updated successfully");
    }

    @PostMapping("/changeGender")
    public ResponseEntity<String> changeGender(@RequestHeader("Authorization") String token,
                                               @RequestBody ChangeGenderRequest request){
        userService.changeGender(token, request.getNewGender());
        return ResponseEntity.ok("Gender updated successfully");
    }

    @PostMapping("/changeBirth")
    public ResponseEntity<String> changeBirth(@RequestHeader("Authorization") String token,
                                               @RequestBody ChangeBirthRequest request){
        userService.changeBirth(token, request.getNewBirth());
        return ResponseEntity.ok("Birth updated successfully");
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadProfileImage(@RequestHeader("Authorization") String token,
                                                     @RequestPart(value = "image") MultipartFile image) {
        try {
            // S3에 이미지 업로드 및 프로필 이미지 저장 처리
            String imageUrl = userService.uploadProfileImage(token, image);
            return ResponseEntity.ok(imageUrl);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }



}
