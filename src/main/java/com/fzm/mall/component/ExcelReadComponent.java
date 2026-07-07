package com.fzm.mall.component;

import com.fzm.mall.constant.ChainConstant;
import com.fzm.mall.constant.enums.CommonEnum;
import com.fzm.mall.constant.enums.GoodsEnum;
import com.fzm.mall.constant.enums.MetaTypeEnum;
import com.fzm.mall.constant.response.ValidateException;
import com.fzm.mall.entity.dataobject.AirdropWhiteDO;
import com.fzm.mall.entity.dataobject.GoodsSkuDO;
import com.fzm.mall.entity.dataobject.GoodsSpuDO;
import com.fzm.mall.entity.dataobject.GoodsWhiteDO;
import com.fzm.mall.mapper.GoodsSkuMapper;
import com.fzm.mall.mapper.GoodsSpuMapper;
import com.fzm.mall.util.AssertUtils;
import com.fzm.mall.util.ParamsUtils;
import lombok.RequiredArgsConstructor;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ExcelReadComponent {
    private final GoodsSpuMapper goodsSpuMapper;
    private final GoodsSkuMapper goodsSkuMapper;

    private static String verifyFile(MultipartFile file) {
        AssertUtils.isNotNull(file, "文件错误");
        AssertUtils.isFalse(file.isEmpty(), "文件错误");

        String filename = file.getOriginalFilename();
        AssertUtils.isNotBlank(filename, "文件错误");

        return filename;
    }

    private static Workbook initWorkbook(String filename, ByteArrayInputStream inputStream) throws IOException {
        if (filename.endsWith(".xls")) {
            return new HSSFWorkbook(inputStream);
        } else if (filename.endsWith(".xlsx")) {
            return new XSSFWorkbook(inputStream);
        }
        throw new ValidateException("文件类型错误");
    }

    private static String getAddress(Row row, int rowNum, int col) {
        try {
            return ParamsUtils.address(getCellString(row.getCell(col)));
        } catch (Exception e) {
            throw new ValidateException("第" + (rowNum + 1) + "行第" + (col + 1) + "列，地址错误");
        }
    }

    private static BigDecimal getBigDecimal(Row row, int rowNum, int col) {
        try {
            return new BigDecimal(getCellString(row.getCell(col)));
        } catch (Exception e) {
            throw new ValidateException("第" + (rowNum + 1) + "行第" + (col + 1) + "列，数量错误");
        }
    }

    private static String getCellString(Cell cell) {
        if (cell == null) {
            return null;
        }
        if (CellType.STRING == cell.getCellType()) {
            return cell.getStringCellValue();
        }
        if (CellType.NUMERIC == cell.getCellType()) {
            return String.valueOf(cell.getNumericCellValue());
        }
        return null;
    }

    private static void close(Workbook workbook) {
        if (workbook != null) {
            try {
                workbook.close();
            } catch (IOException ignored) {

            }
        }
    }

    public List<GoodsWhiteDO> readGoodsWhiteDOS(String goodsId, MultipartFile file) {
        String filename = verifyFile(file);

        Workbook workbook = null;
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(file.getBytes())) {
            workbook = initWorkbook(filename, inputStream);

            Sheet sheet = workbook.getSheetAt(0);
            int lastRowNum = sheet.getLastRowNum();

            // 名单信息
            List<GoodsWhiteDO> whiteDOS = new ArrayList<>(lastRowNum);

            for (int rowNum = 0; rowNum <= lastRowNum; rowNum++) {
                Row row = sheet.getRow(rowNum);
                AssertUtils.isNotNull(row, "第" + (rowNum + 1) + "行错误，空行");

                int col = -1;

                // 地址
                String address = getAddress(row, rowNum, ++col);
                for (GoodsWhiteDO tempDO : whiteDOS) {
                    if (tempDO.getAddress().equals(address)) {
                        throw new ValidateException("第" + (rowNum + 1) + "行第" + (col + 1) + "列，地址重复");
                    }
                }

                // 数量
                int num = getBigDecimal(row, rowNum, ++col).intValue();
                AssertUtils.isTrue(num > 0, "第" + (rowNum + 1) + "行第" + (col + 1) + "列，数量错误");

                GoodsWhiteDO whiteDO = new GoodsWhiteDO();
                whiteDO.setGoodsId(goodsId);
                whiteDO.setSkuId("0");
                whiteDO.setAddress(address);
                whiteDO.setNum(num);

                whiteDOS.add(whiteDO);
            }

            return whiteDOS;
        } catch (ValidateException e) {
            throw e;
        } catch (Exception e) {
            throw new ValidateException("文件错误");
        } finally {
            close(workbook);
        }
    }

    public List<AirdropWhiteDO> readAirdropWhiteDOS(MultipartFile file, String merchantAddress) {
        String filename = verifyFile(file);

        Workbook workbook = null;
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(file.getBytes())) {
            workbook = initWorkbook(filename, inputStream);

            Sheet sheet = workbook.getSheetAt(0);
            int lastRowNum = sheet.getLastRowNum();

            // 名单信息
            List<AirdropWhiteDO> whiteDOS = new ArrayList<>(lastRowNum);

            for (int rowNum = 0; rowNum <= lastRowNum; rowNum++) {
                Row row = sheet.getRow(rowNum);
                AssertUtils.isNotNull(row, "第" + (rowNum + 1) + "行错误，空行");

                int col = -1;

                // 地址
                String address = getAddress(row, rowNum, ++col);

                // 类型
                MetaTypeEnum metaTypeEnum = MetaTypeEnum.exist(getCellString(row.getCell(++col)));
                AssertUtils.isNotNull(metaTypeEnum, "第" + (rowNum + 1) + "行第" + (col + 1) + "列，类型错误");
                AssertUtils.isFalse(metaTypeEnum == MetaTypeEnum.unknown, "第" + (rowNum + 1) + "行第" + (col + 1) + "列，类型错误");

                // 编号
                String metaId = getCellString(row.getCell(++col));
                AssertUtils.isNotBlank(metaId, "第" + (rowNum + 1) + "行第" + (col + 1) + "列，编号错误");

                if (metaTypeEnum == MetaTypeEnum.goods) {
                    GoodsSkuDO skuDO = goodsSkuMapper.getBySkuId(metaId);
                    AssertUtils.isNotNull(skuDO, "第" + (rowNum + 1) + "行第" + (col + 1) + "列，编号错误");
                    AssertUtils.isTrue(skuDO.getAddress().equals(merchantAddress), "第" + (rowNum + 1) + "行第" + (col + 1) + "列，编号错误");

                    GoodsSpuDO spuDO = goodsSpuMapper.getByGoodsId(skuDO.getGoodsId());
                    AssertUtils.isFalse(spuDO.getType() == GoodsEnum.SpuTypeEnum.blind_box.getType() && skuDO.getBlindBoxType() == CommonEnum.BoolEnum.NO.getStatus(), "第" + (rowNum + 1) + "行第" + (col + 1) + "列，盲盒奖品不可空投");
                    AssertUtils.isFalse(spuDO.getType() == GoodsEnum.SpuTypeEnum.synthetic.getType(), "第" + (rowNum + 1) + "行第" + (col + 1) + "列，合成商品不可空投");
                }

                // 数量
                int num = getBigDecimal(row, rowNum, ++col).intValue();
                AssertUtils.isTrue(num > 0 && num <= ChainConstant.multi_batch_transfer_token_max_num_pre_address, "第" + (rowNum + 1) + "行第" + (col + 1) + "列，数量必须在[1," + ChainConstant.multi_batch_transfer_token_max_num_pre_address + "]内");

                AirdropWhiteDO whiteDO = new AirdropWhiteDO();
                whiteDO.setAddress(address);
                whiteDO.setMetaType(metaTypeEnum.getType());
                whiteDO.setMetaId(metaId);
                whiteDO.setNum(num);
                whiteDO.setTokenIdJson("[]");

                whiteDOS.add(whiteDO);
                AssertUtils.isTrue(whiteDOS.size() <= ChainConstant.multi_batch_transfer_max_address, "最多只能同时空投100个地址");
            }

            return whiteDOS;
        } catch (ValidateException e) {
            throw e;
        } catch (Exception e) {
            throw new ValidateException("文件错误");
        } finally {
            close(workbook);
        }
    }
}
