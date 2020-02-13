package fun.sherman.tlmall.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 产品信息
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    private Integer id;
    private Integer categoryId;
    private String name;
    private String subtitle;
    /**
     * 产品主图，url是相对地址
     */
    private String mainImage;
    private String subImages;
    /**
     * 商品详情，富文本内容
     */
    private String detail;
    private BigDecimal price;
    private Integer stock;
    /**
     * 商品状态：1-在售，2-下架，3-删除
     */
    private Integer status;
    private java.util.Date createTime;
    private java.util.Date updateTime;
}
