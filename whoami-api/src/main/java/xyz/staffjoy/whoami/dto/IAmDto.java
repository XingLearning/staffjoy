package xyz.staffjoy.whoami.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.staffjoy.company.dto.AdminOfList;
import xyz.staffjoy.company.dto.WorkerOfList;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IAmDto {
    /**
     * 是否支持者
     */
    private boolean support;
    /**
     * 用户ID
     */
    private String userId;
    /**
     * 工人列表
     */
    private WorkerOfList workerOfList;
    /**
     * 管理列表
     */
    private AdminOfList adminOfList;
}
