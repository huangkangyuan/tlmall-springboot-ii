package fun.sherman.tlmall.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 收货地址
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Shipping {

    private Integer id;
    private Integer userId;
    private String receiverName;
    private String receiverPhone;
    private String receiverMobile;
    private String receiverProvince;
    private String receiverCity;
    private String receiverDistrict;
    private String receiverAddress;
    private String receiverZip;
    private java.util.Date createTime;
    private java.util.Date updateTime;
}
