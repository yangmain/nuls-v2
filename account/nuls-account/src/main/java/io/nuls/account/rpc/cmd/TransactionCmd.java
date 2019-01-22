package io.nuls.account.rpc.cmd;

import com.fasterxml.jackson.databind.DeserializationFeature;
import io.nuls.account.config.NulsConfig;
import io.nuls.account.constant.AccountConstant;
import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.constant.RpcParameterNameConstant;
import io.nuls.account.model.bo.Chain;
import io.nuls.account.model.bo.tx.txdata.Alias;
import io.nuls.account.model.dto.CoinDto;
import io.nuls.account.model.dto.MulitpleAddressTransferDto;
import io.nuls.account.model.po.AliasPo;
import io.nuls.account.service.AliasService;
import io.nuls.account.service.TransactionService;
import io.nuls.account.storage.AliasStorageService;
import io.nuls.account.util.TxUtil;
import io.nuls.account.util.annotation.ResisterTx;
import io.nuls.account.util.annotation.TxMethodType;
import io.nuls.account.util.log.LogUtil;
import io.nuls.account.util.manager.ChainManager;
import io.nuls.account.util.validator.TxValidator;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.Transaction;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.Parameter;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.data.BigIntegerUtils;
import io.nuls.tools.data.StringUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.parse.JSONUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.*;

/**
 * @author: qinyifeng
 * @description:
 * @date: 2018/11/5
 */
@Component
public class TransactionCmd extends BaseCmd {

    @Autowired
    private TransactionService transactionService;
    @Autowired
    private ChainManager chainManager;
    @Autowired
    private TxValidator txValidator;
    @Autowired
    private AliasStorageService aliasStorageService;


    /**
     * 转账交易验证
     */
    @CmdAnnotation(cmd = "ac_transferTxValidate", version = 1.0, description = "create transfer transaction validate 1.0")
    @ResisterTx(txType = AccountConstant.TX_TYPE_TRANSFER, methodType = TxMethodType.VALID, methodName = "ac_transferTxValidate")
    @Parameter(parameterName = RpcParameterNameConstant.CHAIN_ID, parameterType = "int")
    @Parameter(parameterName = RpcParameterNameConstant.TX_HEX, parameterType = "String")
    public Response transferTxValidate(Map<String, Object> params) {
        LogUtil.debug("ac_transferTxValidate start");
        boolean result;
        try {
            if (params.get(RpcParameterNameConstant.CHAIN_ID) == null || params.get(RpcParameterNameConstant.TX_HEX) == null) {
                throw new NulsException(AccountErrorCode.PARAMETER_ERROR);
            }
            int chainId = (Integer) params.get(RpcParameterNameConstant.CHAIN_ID);
            String txHex = (String) params.get(RpcParameterNameConstant.TX_HEX);
            if (chainId <= 0) {
                throw new NulsException(AccountErrorCode.PARAMETER_ERROR);
            }
            Transaction transaction = TxUtil.getTransaction(txHex);
            result = txValidator.validateTx(chainId, transaction);
        } catch (NulsException e) {
            LogUtil.warn("", e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            LogUtil.error("", e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        Map<String, Boolean> resultMap = new HashMap<>();
        resultMap.put("value", result);
        LogUtil.debug("ac_transferTxValidate end");
        return success(resultMap);
    }

    /**
     * 转账交易提交
     */
    @CmdAnnotation(cmd = "ac_transferTxCommit", version = 1.0, description = "create transfer transaction commit 1.0")
    @ResisterTx(txType = AccountConstant.TX_TYPE_TRANSFER, methodType = TxMethodType.COMMIT, methodName = "ac_transferTxCommit")
    public Response transferTxCommit(Map<String, Object> params) {
        return success();
    }

    /**
     * 转账交易回滚
     */
    @CmdAnnotation(cmd = "ac_transferTxRollback", version = 1.0, description = "create transfer transaction rollback 1.0")
    @ResisterTx(txType = AccountConstant.TX_TYPE_TRANSFER, methodType = TxMethodType.ROLLBACK, methodName = "ac_transferTxRollback")
    public Response transferTxRollback(Map<String, Object> params) {
        return success();
    }

    /**
     * 创建多账户转账交易
     * create a multi-account transfer transaction
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "ac_multipleAddressTransfer", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "create a multi-account transfer transaction")
    public Response multipleAddressTransfer(Map params) {
        LogUtil.debug("ac_multipleAddressTransfer start");
        Map<String, String> map = new HashMap<>(1);
        try {
            // check parameters
            if (params == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }

            // parse params
            JSONUtils.getInstance().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            MulitpleAddressTransferDto transferDto = JSONUtils.json2pojo(JSONUtils.obj2json(params), MulitpleAddressTransferDto.class);
            List<CoinDto> inputList = transferDto.getInputs();
            List<CoinDto> outputList = transferDto.getOutputs();
            if (inputList == null || outputList == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }

            // check address validity
            BigInteger fromTotal = BigInteger.ZERO;
            for (CoinDto from : inputList) {
                if (!AddressTool.validAddress(from.getAssetsChainId(), from.getAddress())) {
                    throw new NulsException(AccountErrorCode.ADDRESS_ERROR);
                }
                //转出账户列表中不能有多签账户
                //from中不能有多签地址
                if (AddressTool.isMultiSignAddress(from.getAddress())) {
                    throw new NulsException(AccountErrorCode.IS_MULTI_SIGNATURE_ADDRESS);
                }
                fromTotal = fromTotal.add(from.getAmount());
            }
            BigInteger toTotal = BigInteger.ZERO;
            for (CoinDto to : outputList) {
                if (!AddressTool.validAddress(to.getAssetsChainId(), to.getAddress())) {
                    throw new NulsException(AccountErrorCode.ADDRESS_ERROR);
                }
                toTotal = toTotal.add(to.getAmount());
            }

            // check transfer amount
            if (BigIntegerUtils.isLessThan(fromTotal, BigInteger.ZERO) || BigIntegerUtils.isLessThan(toTotal, BigInteger.ZERO)) {
                throw new NulsException(AccountErrorCode.PARAMETER_ERROR);
            }
            // check transaction remark
            if (!validTxRemark(transferDto.getRemark())) {
                throw new NulsException(AccountErrorCode.PARAMETER_ERROR);
            }
            String txDigestHex = transactionService.multipleAddressTransfer(transferDto.getChainId(), inputList, outputList, transferDto.getRemark());
            map.put("value", txDigestHex);
        } catch (NulsException e) {
            return failed(e.getErrorCode());
        } catch (NulsRuntimeException e) {
            return failed(e.getErrorCode());
        } catch (IOException e) {
            return failed(e.getMessage());
        } catch (Exception e) {
            return failed(e.getMessage());
        }
        LogUtil.debug("ac_multipleAddressTransfer end");
        return success(map);
    }

    /**
     * 创建别名转账交易
     *
     * create the transaction of transfer by alias
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "ac_transferByAlias", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "transfer by alias")
    public Response transferByAlias(Map params) {
        LogUtil.debug("ac_transferByAlias start");
        Map<String, String> map = new HashMap<>(1);
        Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
        Object addressObj = params == null ? null : params.get(RpcParameterNameConstant.TX_HEX);
        Object passwordObj = params == null ? null : params.get(RpcParameterNameConstant.SECONDARY_DATA_Hex);
        Object aliasObj = params == null ? null : params.get(RpcParameterNameConstant.SECONDARY_DATA_Hex);
        Object amountObj = params == null ? null : params.get(RpcParameterNameConstant.SECONDARY_DATA_Hex);
        Object remarkObj = params == null ? null : params.get(RpcParameterNameConstant.SECONDARY_DATA_Hex);
        try {
            // check parameters
            if (params == null || chainIdObj == null || addressObj == null || passwordObj == null || aliasObj == null ||
                    amountObj == null || remarkObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            int chainId = (int) chainIdObj;
            String address = (String) addressObj;
            String password = (String) passwordObj;
            String alias = (String) aliasObj;
            BigInteger amount = new BigInteger((String) amountObj);
            String remark = (String) remarkObj;
            if (BigIntegerUtils.isLessThan(amount, BigInteger.ZERO)) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            if (!validTxRemark(remark)) { // check transaction remark
                throw new NulsException(AccountErrorCode.PARAMETER_ERROR);
            }
            AliasPo aliasPo = aliasStorageService.getAlias(chainId,alias); //根据别名查询出地址
            if (aliasPo == null) {
                throw new NulsRuntimeException(AccountErrorCode.ALIAS_NOT_EXIST);
            }
            Chain chain = chainManager.getChainMap().get(chainId);
            if (chain == null) {
                throw new NulsRuntimeException(AccountErrorCode.CHAIN_NOT_EXIST);
            }
            int assetId = chain.getConfig().getAssetsId();
            CoinDto fromCoinDto = new CoinDto();
            CoinDto toCoinDto = new CoinDto();
            fromCoinDto.setAddress(address);
            fromCoinDto.setAmount(amount);
            fromCoinDto.setAssetsChainId(chainId);
            fromCoinDto.setAssetsId(assetId);
            fromCoinDto.setPassword(password);

            toCoinDto.setAddress(AddressTool.getStringAddressByBytes(aliasPo.getAddress()));
            toCoinDto.setAmount(amount);
            fromCoinDto.setAssetsChainId(chainId);
            fromCoinDto.setAssetsId(assetId);
            Transaction tx = transactionService.transferByAlias(chainId, fromCoinDto, toCoinDto, remark);
            map.put("txHash", tx.getHash().getDigestHex());
        } catch (NulsException e) {
            return failed(e.getErrorCode());
        } catch (NulsRuntimeException e) {
            return failed(e.getErrorCode());
        } catch (Exception e) {
            return failed(e.getMessage());
        }
        LogUtil.debug("ac_multipleAddressTransfer end");
        return success(map);
    }

    /**
     * 校验转账交易备注是否有效
     *
     * @param remark
     * @return
     */
    private boolean validTxRemark(String remark) {
        if (StringUtils.isBlank(remark)) {
            return true;
        }
        try {
            byte[] bytes = remark.getBytes(NulsConfig.DEFAULT_ENCODING);
            if (bytes.length > 100) {
                return false;
            }
            return true;
        } catch (UnsupportedEncodingException e) {
            return false;
        }
    }
}
