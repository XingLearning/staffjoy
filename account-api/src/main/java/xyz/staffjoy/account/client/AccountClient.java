package xyz.staffjoy.account.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import xyz.staffjoy.account.AccountConstant;
import xyz.staffjoy.account.dto.*;
import xyz.staffjoy.common.api.BaseResponse;
import xyz.staffjoy.common.auth.AuthConstant;
import xyz.staffjoy.common.validation.Group1;
import xyz.staffjoy.common.validation.PhoneNumber;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@FeignClient(name = AccountConstant.SERVICE_NAME, path = "/v1/account", url = "${staffjoy.account-service-endpoint}")
// TODO Client side validation can be enabled as needed
// @Validated
public interface AccountClient {

    /**
     * 创建新账户
     * @param authz
     * @param request
     * @return
     */
    @PostMapping(path = "/create")
    GenericAccountResponse createAccount(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestBody @Valid CreateAccountRequest request);

    /**
     * 同步用户事件到Intercon客服系统
     * @param request
     * @return
     */
    @PostMapping(path = "/track_event")
    BaseResponse trackEvent(@RequestBody @Valid TrackEventRequest request);

    /**
     * 同步信息到Intercon客户系统
     * @param request
     * @return
     */
    @PostMapping(path = "/sync_user")
    BaseResponse syncUser(@RequestBody @Valid SyncUserRequest request);

    /**
     * 获取现有账户列表（内部使用）
     * @param authz
     * @param offset
     * @param limit
     * @return
     */
    @GetMapping(path = "/list")
    ListAccountResponse listAccounts(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestParam int offset, @RequestParam @Min(0) int limit);

    /**
     * 获取或创建（如不存在）账户
     * @param authz
     * @param request
     * @return
     */
    // GetOrCreate is for internal use by other APIs to match a user based on their phonenumber or email.
    @PostMapping(path= "/get_or_create")
    GenericAccountResponse getOrCreateAccount(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestBody @Valid GetOrCreateRequest request);

    /**
     * 通过用户id获取已有账户
     * @param authz
     * @param userId
     * @return
     */
    @GetMapping(path = "/get")
    GenericAccountResponse getAccount(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestParam @NotBlank String userId);

    /**
     * 更新账户
     * @param authz
     * @param newAccount
     * @return
     */
    @PutMapping(path = "/update")
    GenericAccountResponse updateAccount(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestBody @Valid AccountDto newAccount);

    /**
     * 通过电话号码获取已有账户
     * @param authz
     * @param phoneNumber
     * @return
     */
    @GetMapping(path = "/get_account_by_phonenumber")
    GenericAccountResponse getAccountByPhonenumber(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestParam @PhoneNumber String phoneNumber);

    /**
     * 更新密码
     * @param authz
     * @param request
     * @return
     */
    @PutMapping(path = "/update_password")
    BaseResponse updatePassword(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestBody @Valid UpdatePasswordRequest request);

    /**
     * 检验密码
     * @param authz
     * @param request
     * @return
     */
    @PostMapping(path = "/verify_password")
    GenericAccountResponse verifyPassword(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestBody @Valid VerifyPasswordRequest request);

    /**
     * 请求密码重置
     * @param authz
     * @param request
     * @return
     */
    // RequestPasswordReset sends an email to a user with a password reset link
    @PostMapping(path = "/request_password_reset")
    BaseResponse requestPasswordReset(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestBody @Valid PasswordResetRequest request);

    @PostMapping(path = "/request_email_change")
    BaseResponse requestEmailChange(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestBody @Valid EmailChangeRequest request);

    // ChangeEmail sets an account to active and updates its email. It is
    // used after a user clicks a confirmation link in their email.
    @PostMapping(path = "/change_email")
    BaseResponse changeEmail(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestBody @Valid EmailConfirmation request);
}
