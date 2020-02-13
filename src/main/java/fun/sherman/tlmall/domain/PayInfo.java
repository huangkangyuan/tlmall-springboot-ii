package fun.sherman.tlmall.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 支付信息
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PayInfo {

    private Integer id;
    private Integer userId;
    private Long orderNo;
    /**
     * 支付平台：1-支付宝，2-微信
     */
    private Integer payPlatform;
    /**
     * 支付状态返回码，原生平台状态信息
     */
    private String platformStatus;
    /**
     * 支付宝支付流水号
     */
    private String platformNumber;
    private java.util.Date createTime;
    private java.util.Date updateTime;
}
