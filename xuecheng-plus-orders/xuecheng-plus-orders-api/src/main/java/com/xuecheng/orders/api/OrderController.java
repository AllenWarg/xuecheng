package com.xuecheng.orders.api;


import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayConfig;
import com.xuecheng.orders.model.dto.AddOrderDto;
import com.xuecheng.orders.model.dto.PayRecordDto;
import com.xuecheng.orders.service.OrderService;
import com.xuecheng.orders.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Api(value = "订单支付接口", tags = "订单支付接口")
@Slf4j
@Controller
public class OrderController {
    @Autowired
    AlipayConfig alipayConfig;
    @Autowired
    OrderService orderService;

    @ApiOperation("生成支付二维码")
    @ResponseBody
    @PostMapping("/generatepaycode")
    public PayRecordDto generatepaycode(@RequestBody AddOrderDto addOrderDto,HttpServletRequest request){
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        Long userId = Long.valueOf(user.getId());
        return orderService.generatePayOrder(addOrderDto,userId);
    }


    @ApiOperation("扫码下单接口")
    @GetMapping("/requestpay")
    public void requestpay(String payNo, HttpServletResponse httpResponse) throws IOException {
        String res = orderService.payOrder(payNo);
        httpResponse.setContentType("text/html;charset=" + alipayConfig.getCharset());
        httpResponse.getWriter().write(res);
        httpResponse.getWriter().flush();
    }

    @ApiOperation("查询支付结果")
    @GetMapping("/payresult")
    @ResponseBody
    public PayRecordDto payresult(String payNo) throws IOException {

        //查询支付结果

        return orderService.queryPayResult(payNo);

    }

    /**
     * 支付宝支付通知
     * @param request
     * @param response
     */
    @RequestMapping("/alipayNotify")
    public void alipayNotify(HttpServletRequest request, HttpServletResponse response) throws AlipayApiException, UnsupportedEncodingException {
        Map<String, String> params = new HashMap<String, String>();
        Map requestParams = request.getParameterMap();
        for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
            // valueStr = new String(valueStr.getBytes("ISO-8859-1"), "UTF-8");
            params.put(name, valueStr);
        }
        orderService.alipayNotify(params);
    }

}
