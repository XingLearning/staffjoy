package xyz.staffjoy.account.dto;

import lombok.*;
import xyz.staffjoy.common.api.BaseResponse;

/**
 * 通用账号返回 response
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class GenericAccountResponse extends BaseResponse {
    private AccountDto account;
}
