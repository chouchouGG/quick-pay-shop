package cn.learn.domain.res;

import cn.learn.common.constants.Constants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PayOrderRes {

    private String userId;

    private String orderId;

    private String payUrl;

    private Constants.OrderStatus orderStatus;

}
