package com.fzm.mall.component;

import com.fzm.mall.constant.enums.OrderEnum;
import com.fzm.mall.entity.dataobject.GoodsSkuDO;
import com.fzm.mall.entity.dataobject.OrderExpressDO;
import com.fzm.mall.mapper.GoodsSkuMapper;
import com.fzm.mall.util.TimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ExcelWriteComponent {
    private final GoodsSkuMapper goodsSkuMapper;

    public HttpHeaders getHttpHeaders() {
        String filename = TimeUtils.nowTimestamp() + ".xlsx";

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
        headers.add(HttpHeaders.PRAGMA, "no-cache");
        headers.add(HttpHeaders.EXPIRES, "0");
        headers.add(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);
        return headers;
    }

    public byte[] writeExcelOrderExpressList(List<OrderExpressDO> dos) {

        try (SXSSFWorkbook workbook = new SXSSFWorkbook()) {
            SXSSFSheet sheet = workbook.createSheet();

            int i = 0;
            SXSSFRow row = sheet.createRow(i);
            int col = -1;
            row.createCell(++col).setCellValue("Wallet Address");
            row.createCell(++col).setCellValue("Order ID");
            row.createCell(++col).setCellValue("Goods ID");
            row.createCell(++col).setCellValue("Goods Name");
            row.createCell(++col).setCellValue("Token ID");
            row.createCell(++col).setCellValue("Number");

            row.createCell(++col).setCellValue("Recipient Name");
            row.createCell(++col).setCellValue("Phone Number ");
            row.createCell(++col).setCellValue("Delivery Address");
            row.createCell(++col).setCellValue("Remarks");

            row.createCell(++col).setCellValue("Logistics Company");
            row.createCell(++col).setCellValue("Tracking Number");
            row.createCell(++col).setCellValue("Order Status");
            row.createCell(++col).setCellValue("Apply Time");
            row.createCell(++col).setCellValue("Express Time");
            row.createCell(++col).setCellValue("Confirm Time");

            int size = dos.size();
            for (; i < size; i++) {
                OrderExpressDO obj = dos.get(i);

                GoodsSkuDO skuDO = goodsSkuMapper.getBySkuId(obj.getSkuId());

                row = sheet.createRow(i + 1);
                col = -1;
                row.createCell(++col).setCellValue(obj.getBuyerAddress());
                row.createCell(++col).setCellValue(obj.getOrderId());
                row.createCell(++col).setCellValue(obj.getSkuId());
                row.createCell(++col).setCellValue(skuDO.getName());
                row.createCell(++col).setCellValue(obj.getTokenIdJson());
                row.createCell(++col).setCellValue(obj.getNum());

                row.createCell(++col).setCellValue(obj.getName());
                row.createCell(++col).setCellValue(obj.getPhone());
                row.createCell(++col).setCellValue(obj.getDetail());
                row.createCell(++col).setCellValue(obj.getNote());

                row.createCell(++col).setCellValue("");
                row.createCell(++col).setCellValue(obj.getExpressCode());
                row.createCell(++col).setCellValue(OrderEnum.ExpressStatusEnum.exist(obj.getStatus()).getMessage());
                row.createCell(++col).setCellValue(TimeUtils.timestampMilliToDateTime(obj.getOrderTime()).format(TimeUtils.yyyyMMddHHmmss));
                row.createCell(++col).setCellValue(obj.getExpressTime() == null ? "" : TimeUtils.timestampMilliToDateTime(obj.getExpressTime()).format(TimeUtils.yyyyMMddHHmmss));
                row.createCell(++col).setCellValue(obj.getConfirmTime() == null ? "" : TimeUtils.timestampMilliToDateTime(obj.getConfirmTime()).format(TimeUtils.yyyyMMddHHmmss));
            }

            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                workbook.write(out);
                return out.toByteArray();
            }
        } catch (Exception e) {
            log.error("Write Order Express List Error.", e);
            return null;
        }
    }

}
