package net.northern.crm.persistence.services;

import com.intuit.ipp.core.Context;
import com.intuit.ipp.core.IEntity;
import com.intuit.ipp.core.ServiceType;
import com.intuit.ipp.data.Error;
import com.intuit.ipp.data.*;
import com.intuit.ipp.exception.FMSException;
import com.intuit.ipp.exception.InvalidTokenException;
import com.intuit.ipp.security.OAuth2Authorizer;
import com.intuit.ipp.services.*;
import com.intuit.ipp.util.Config;
import com.intuit.oauth2.client.OAuth2PlatformClient;
import com.intuit.oauth2.config.Environment;
import com.intuit.oauth2.config.OAuth2Config;
import com.intuit.oauth2.data.BearerTokenResponse;
import com.intuit.oauth2.exception.OAuthException;
import com.sun.istack.NotNull;
import net.northern.crm.config.EnvConfig;
import net.northern.crm.config.QuickBooksConfig;
import net.northern.crm.persistence.dto.QBItemLite;
import net.northern.crm.persistence.repositories.SettingsRepository;
import net.northern.crm.persistence.entities.SettingEntity;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class QuickBooksService {
    public static final String KEY_REALM_ID = "realmId";
    public static final String KEY_AUTH_CODE = "auth_code";
    public static final String KEY_ACCESS_TOKEN = "access_token";
    public static final String KEY_REFRESH_TOKEN = "refresh_token";
    public static final String KEY_CSRF_TOKEN = "csrfToken";
    private final Map<String, String> cache = new HashMap<>();
    private final OAuth2PlatformClientFactory factory;
    private final SettingsRepository settingsRepository;
    private final NotificationService notificationService;
    private final EnvConfig envConfig;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public interface QuickBooksAction<T> {
        T doAction(InterceptingDataService dataService) throws FMSException;
    }

    public interface QBItemHost {
        QBItemLite getQbItem();

        double getQuantity();

        //LocationEntity getOrigin();
    }

    @Autowired
    public QuickBooksService(SettingsRepository settingsRepository, QuickBooksConfig quickBooksConfig, NotificationService notificationService, EnvConfig envConfig) {
        this.notificationService = notificationService;
        this.envConfig = envConfig;
        this.factory = new OAuth2PlatformClientFactory(quickBooksConfig);
        this.settingsRepository = settingsRepository;
    }

    /*public void makeBillFromPo(ReceivedEntity updatedObject) {
        //Get PO
        PurchaseOrder purchaseOrder = getPoByNumber(updatedObject.getPo());

        if (purchaseOrder != null) {
            //We have a PO, make a bill with the same info, link them, and add items from receive to the bill
            //TODO New thread for sure
            HashMap<String, ReceiveItemEntity> index = updatedObject.createIndexMap();

            if (purchaseOrder.getLinkedTxn() == null || purchaseOrder.getLinkedTxn().isEmpty()) {
                //Make new bill
                Bill bill = new Bill();

                //Copy basic attributes
                bill.setVendorRef(purchaseOrder.getVendorRef());
                bill.setCurrencyRef(purchaseOrder.getCurrencyRef());
                bill.setGlobalTaxCalculation(purchaseOrder.getGlobalTaxCalculation());
                bill.setSalesTermRef(purchaseOrder.getSalesTermRef());
                bill.setPrivateNote("Bill created automatically by CNC-MRP");
                bill.setDepartmentRef(purchaseOrder.getDepartmentRef());

                //Create a reference to the PO
                LinkedTxn poReference = new LinkedTxn();
                poReference.setTxnId(purchaseOrder.getId());
                poReference.setTxnType(TxnTypeEnum.PURCHASE_ORDER.value());
                bill.setLinkedTxn(List.of(poReference));

                //Now add line items
                List<Line> linesFromPo = new LinkedList<>();

                purchaseOrder.getLine().forEach(poLine -> {
                    if (poLine.getDetailType() == LineDetailTypeEnum.ITEM_BASED_EXPENSE_LINE_DETAIL) {
                        Line newBillLine = new Line();
                        ItemBasedExpenseLineDetail poExpenseLineDetail = poLine.getItemBasedExpenseLineDetail();
                        ItemBasedExpenseLineDetail newBillExpenseLineDetail = new ItemBasedExpenseLineDetail();
                        ReceiveItemEntity receiveItemEntity = index.get(poExpenseLineDetail.getItemRef().getValue());

                        //Remove it from the index, so we can see what's left after
                        index.remove(poExpenseLineDetail.getItemRef().getValue());

                        BigDecimal amountReceived = BigDecimal.valueOf(receiveItemEntity.getQuantity());

                        //Set received on po line & set qty on bill.
                        newBillExpenseLineDetail.setQty(amountReceived);
                        //poLine.setReceived(amountReceived);

                        //Set the item ref
                        newBillExpenseLineDetail.setItemRef(poExpenseLineDetail.getItemRef());
                        newBillExpenseLineDetail.setUnitPrice(poExpenseLineDetail.getUnitPrice());

                        //Set the tax info
                        newBillExpenseLineDetail.setTaxCodeRef(poExpenseLineDetail.getTaxCodeRef());
                        newBillExpenseLineDetail.setTaxClassificationRef(poExpenseLineDetail.getTaxClassificationRef());

                        //Set remaining attributes
                        newBillLine.setDescription(poLine.getDescription());
                        newBillExpenseLineDetail.setCustomerRef(poExpenseLineDetail.getCustomerRef());
                        newBillExpenseLineDetail.setClassRef(poExpenseLineDetail.getClassRef());

                        //Set the line detail
                        newBillLine.setDetailType(LineDetailTypeEnum.ITEM_BASED_EXPENSE_LINE_DETAIL);
                        newBillLine.setItemBasedExpenseLineDetail(newBillExpenseLineDetail);

                        //Assign a line number for the new bill line
                        newBillLine.setLineNum(poLine.getLineNum());

                        //Link line to po
                        LinkedTxn poLineRef = new LinkedTxn();
                        poLineRef.setTxnId(purchaseOrder.getId());
                        poLineRef.setTxnType(TxnTypeEnum.PURCHASE_ORDER.value());
                        poLineRef.setTxnLineId(String.valueOf(poLine.getLineNum()));

                        newBillLine.setLinkedTxn(List.of(poLineRef));

                        newBillLine.setAmount(newBillExpenseLineDetail.getQty().multiply(poExpenseLineDetail.getUnitPrice()));

                        linesFromPo.add(newBillLine);
                    } else {
                        //It's something else, add it without modification
                        linesFromPo.add(poLine);
                    }
                });

                bill.setLine(linesFromPo);

                save(bill);

            } else {
                //Something is already linked to the po

                //Now update the qty on the bill
                Bill bill = getBillById(purchaseOrder.getLinkedTxn().get(0).getTxnId());
                bill.getLine().forEach(line -> {
                    if (line.getDetailType() == LineDetailTypeEnum.ITEM_BASED_EXPENSE_LINE_DETAIL) {
                        ItemBasedExpenseLineDetail billLineDetail = line.getItemBasedExpenseLineDetail();

                        //If null then it is not in this receive, so we will not modify
                        ReceiveItemEntity receiveItemEntity = index.get(billLineDetail.getItemRef().getValue());

                        if (receiveItemEntity != null) {
                            //Remove from index now that we are done with that object
                            index.remove(billLineDetail.getItemRef().getValue());

                            billLineDetail.setQty(billLineDetail.getQty().add(BigDecimal.valueOf(receiveItemEntity.getQuantity())));
                            line.setAmount(billLineDetail.getQty().multiply(billLineDetail.getUnitPrice()));
                        }
                    }
                });

                //Clear tax amounts as to allow QB to recalculate
                bill.setTxnTaxDetail(null);

                save(bill);

            }

            //These are the leftovers, so make a notification for now
            if (index.size() > 0) {

                notificationService.createNotification("Additional Items Received",
                        "Items were received for a shipment (name: <a href=\"/log/received/" + updatedObject.getReceiveId() + "\">" + updatedObject.getName() + "</a>) for a P.O. (po: " + purchaseOrder.getDocNumber() + ") that were not present on the P.O. Please review and manually adjust",
                        String.valueOf(updatedObject.getReceiveId()),
                        NotificationEntity.NotiType.EXTRA_PO_ITEMS);
            }

        } else {
            //Has PO but was not found, so make a notification
            notificationService.createNotification("Bad P.O. Reference",
                    //"A received (name: " + updatedObject.getName() + " &bull; " + updatedObject.getReceiveId() + ") purchase order (" + updatedObject.getPo() + ") referenced by " + updatedObject.getUser().getUsername() + " could not be found",
                    updatedObject.getUser().getUsername() + " received a shipment (name: <a href=\"/log/received/" + updatedObject.getReceiveId() + "\">" + updatedObject.getName() + "</a>) " +
                            "and referenced a purchase order (po: " + updatedObject.getPo() + ") but no purchase order of that name can be found.",
                    updatedObject.getPo(), NotificationEntity.NotiType.BAD_PO);
        }
    }*/

    public IEntity save(IEntity entity) {
        return callQuickBooks(dataService -> dataService.add(entity));
    }

    public PurchaseOrder getPoByNumber(String po) {
        QueryResult result = callQuickBooks(dataService -> dataService.executeQuery(formatSQL("select * from PurchaseOrder where DocNumber=?", po)));

        if (result == null) {
            return null;
        }

        if (result.getEntities().size() > 1) {
            logger.error("Multiple PO's found for single doc number: " + po);
        } else if (result.getEntities().isEmpty()) {
            logger.error("No PO found for doc number: " + po);
            return null;
        }

        return ((PurchaseOrder) result.getEntities().get(0));
    }

    public Item getItemById(String qbId) {
        QueryResult result = callQuickBooks(dataService -> dataService.executeQuery(formatSQL("select * from Item where Id=?", qbId)));

        assert result != null;
        return (Item) result.getEntities().get(0);
    }

    public Bill getBillById(String txnId) {
        QueryResult result = callQuickBooks(dataService -> dataService.executeQuery(formatSQL("select * from bill where Id=?", txnId)));

        if (result == null) {
            return null;
        }

        if (result.getEntities().size() > 1) {
            logger.error("Multiple bills found for single id number: " + txnId);
        } else if (result.getEntities().isEmpty()) {
            logger.error("No bill found for id number: " + txnId);
            return null;
        }

        return ((Bill) result.getEntities().get(0));
    }

    public List<QBItemLite> getAllItems() {
        QueryResult result = callQuickBooks(dataService -> dataService.executeQuery("select * from Item where Type in ('Inventory', 'NonInventory')"));

        ArrayList<QBItemLite> products = new ArrayList<>();

        if (result != null && !result.getEntities().isEmpty()) {
            for (IEntity entity : result.getEntities()) {
                Item item = (Item) entity;
                products.add(new QBItemLite(item));
            }

            return products;

        } else {
            return null;
        }
    }

    public List<QBItemLite> getItemByFqnQuery(String query) {
        QueryResult result = callQuickBooks(dataService -> dataService.executeQuery(formatSQL("select * from Item where Type in ('Inventory', 'NonInventory') and FullyQualifiedName like ?", "%" + query.replace(" ", "%") + "%")));
        LinkedList<QBItemLite> items = new LinkedList<>();

        if (result == null) {
            return new LinkedList<>();
        }

        for (IEntity entity : result.getEntities()) {
            items.add(new QBItemLite((Item) entity));
        }

        return items;

    }

   /* *//**
     * Helper method that implies adding to inventory
     *
     * @param qbItemHosts A list of objects that can provide a {@link com.cccandle.cncmrp.persistence.entities.ReceiveItemEntity.QBItemLite QBItemLite} and a quantity to increment/decrement
     *//*
    public void completeReceive(List<? extends QBItemHost> qbItemHosts) {
        adjustInventory(qbItemHosts, true);
    }*/

    /**
     * Will adjust inventory in quickbooks by using a batch update
     *
     * @param qbItemHosts A list of objects that can provide a {@link net.northern.crm.persistence.dto.QBItemLite QBItemLite} and a quantity to increment/decrement
     * @param addToInv    True will add to inventory and false will subtract
     */
    public void adjustInventory(List<? extends QBItemHost> qbItemHosts, boolean addToInv) {
        //Change this to get a fresh object first, then update

        callQuickBooks(dataService -> {

            //This will be used to update all the items in the shipment
            BatchOperation updateOperation = new BatchOperation();

            //This will be used to easily get the details.
            Map<String, QBItemHost> itemsToUpdate = new HashMap<>(qbItemHosts.size());

            //Used for constructing SQL
            StringBuilder idValues = new StringBuilder();

            qbItemHosts.forEach(itemHost -> {
                //Add mapping to map
                itemsToUpdate.put(itemHost.getQbItem().getQbId(), itemHost);

                //Construct values
                idValues.append("'").append(itemHost.getQbItem().getQbId()).append("',");
            });

            //Execute SQL to grab all new objects
            dataService.executeQueryAsync("select * from Item where Id in ("
                    + idValues.substring(0, idValues.length() - 1) + ")", callbackMessage -> {

                //Deal with each result
                for (IEntity item : callbackMessage.getQueryResult().getEntities()) {
                    Item freshQuickbooksItem = (Item) item;

                    BigDecimal currentAmount = freshQuickbooksItem.getQtyOnHand();
                    BigDecimal quantity = new BigDecimal(String.valueOf(
                            itemsToUpdate.get(freshQuickbooksItem.getId()).getQuantity()
                    ));

                    //Add or subtract based on flag
                    freshQuickbooksItem.setQtyOnHand(addToInv ? currentAmount.add(quantity) : currentAmount.subtract(quantity));

                    //Add update to batch
                    updateOperation.addEntity(freshQuickbooksItem, OperationEnum.UPDATE, freshQuickbooksItem.getId());

                }

                try {
                    //Execute batch
                    dataService.executeBatchAsync(updateOperation, this::parseCallbackMessage);
                } catch (FMSException e) {
                    throw new RuntimeException(e);
                }
            });

            //Return null because we don't care about returned results for the root callback
            return null;
        });
    }

    //Here are the helper methods for calling and storing settings.

    /**
     * Stores a setting in the DB and updates cache
     *
     * @param key   Key for the setting
     * @param value Value for the setting
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void storeSetting(String key, String value) {
        Optional<SettingEntity> setting = settingsRepository.findById(key);

        if (setting.isPresent()) {
            setting.get().setData(value);
            settingsRepository.save(setting.get());
        } else {
            SettingEntity newSetting = new SettingEntity();
            newSetting.setSetting(key);
            newSetting.setData(value);

            settingsRepository.save(newSetting);
        }

        cache.put(key, value);
    }

    /**
     * Retrieves setting from cache or database if it is not present in the cache
     *
     * @param key Key for the setting
     * @return The value for said setting
     */
    public String getSetting(String key) {
        if (cache.containsKey(key) && envConfig.isEnableInMemorySettingsCache()) {
            return cache.get(key);
        } else {
            Optional<SettingEntity> setting = settingsRepository.findById(key);
            if (setting.isPresent()) {
                cache.put(key, setting.get().getData());
                return setting.get().getData();
            }
        }

        return null;
    }

    private InterceptingDataService getDataService(String realmId, String accessToken) throws FMSException {

        String url = factory.getAccountingAPIHost() + "/v3/company";

        Config.setProperty(Config.BASE_URL_QBO, url);
        //create oauth object
        OAuth2Authorizer oauth = new OAuth2Authorizer(accessToken);
        //create context
        Context context = new Context(oauth, ServiceType.QBO, realmId);

        // create dataservice
        return new InterceptingDataService(context);
    }

    public static String formatSQL(String sql, @NotNull Object... arg) {
        String[] brokenSql = sql.split("\\?");

        StringBuilder builder = new StringBuilder();

        for (int x = 0; x < arg.length; x++) {
            builder.append(brokenSql[x]).append("'").append(String.valueOf(arg[x])

                            //remove any ;. There is no reason for these to be here
                            .replace(";", "")

                            //These can have a reason, so simply escape them
                            .replace("'", "\\'")
                            .replace("\"", "\\\""))

                    //Add last encapsulating quote
                    .append("'");
        }

        return builder.toString();
    }

    /**
     * Sample QBO API call using OAuth2 tokens
     *
     * @param action Code to run with the data service
     */
    private <T> T callQuickBooks(QuickBooksAction<T> action) {

        String realmId = getSetting(KEY_REALM_ID);
        if (StringUtils.isEmpty(realmId)) {
            return null;
        }

        try {
            //get DataService
            InterceptingDataService service = getDataService(realmId, getSetting(KEY_ACCESS_TOKEN));

            //Setting the action allows for a retry of async requests
            service.setAction(action);

            return action.doAction(service);
            //return service.executeQuery(sql);

        } catch (InvalidTokenException e) {
            /*
             * Handle 401 status code -
             * If a 401 response is received, refresh tokens should be used to get a new access token,
             * and the API call should be tried again.
             */

            logger.error("Error while calling executeQuery (InvalidToken Exception) :: " + e.getMessage());

            return refreshTokens(realmId, action);

        } catch (FMSException e) {
            List<Error> list = e.getErrorList();
            list.forEach(error -> {
                logger.error("Error while calling executeQuery (FMS Exception) :: " + error.getMessage() + " :: " + error.getDetail());
                //logger.error("Exception message: " + e.getMessage());
            });

            if (e.getMessage().contains("statusCode=401")) {
                //Try to refresh tokens
                return refreshTokens(realmId, action);
            }
        } catch (Exception e) {
            //Because quickbooks seems to be throwing a random fucking exceptions
            e.printStackTrace();

            if (e.getMessage().contains("statusCode=401")) {
                //Try to refresh tokens
                return refreshTokens(realmId, action);
            }
        }

        return null;
    }

    private <T> T refreshTokens(String realmId, QuickBooksAction<T> action) {
        //refresh tokens
        logger.info("received 401 during call, refreshing tokens now");
        OAuth2PlatformClient client = factory.getOAuth2PlatformClient();
        String refreshToken = getSetting(KEY_REFRESH_TOKEN);

        try {
            BearerTokenResponse bearerTokenResponse = client.refreshToken(refreshToken);

            String accessToken = bearerTokenResponse.getAccessToken();
            refreshToken = bearerTokenResponse.getRefreshToken();

            storeSetting(KEY_ACCESS_TOKEN, accessToken);
            storeSetting(KEY_REFRESH_TOKEN, refreshToken);

            //call again using new tokens
            logger.info("calling using new tokens");
            InterceptingDataService service = getDataService(realmId, accessToken);

            return action.doAction(service);

        } catch (OAuthException e1) {
            logger.error("Error while calling bearer token :: " + e1.getMessage());
            return null;
        } catch (FMSException e1) {
            logger.error("Error while calling company currency :: " + e1.getMessage());
            return null;
        }
    }

    public void parseCallbackMessage(CallbackMessage message) {
        logger.info("Message from batch operation:");

        BatchOperation batchOperation = message.getBatchOperation();
        List<String> bIds = batchOperation.getBIds();

        for (String bId : bIds) {

            if (batchOperation.isFault(bId)) {
                Fault fault = batchOperation.getFault(bId);
                Error error = fault.getError().get(0);
                logger.info("Fault error :" + error.getCode() + ", " + error.getDetail() + ", " + error.getMessage());

            } else if (batchOperation.isEntity(bId)) {
                logger.info("Entity : " + ((Customer) batchOperation.getEntity(bId)).getDisplayName());

            } else if (batchOperation.isQuery(bId)) {
                QueryResult queryResult = batchOperation.getQueryResponse(bId);
                logger.info("Query : " + queryResult.getTotalCount());

            } else if (batchOperation.isReport(bId)) {
                Report report = batchOperation.getReport(bId);
                logger.info("Report : " + report.getHeader().getReportName());

            } else {
                logger.error("Something wrong!...");
            }
        }
    }

    public OAuth2PlatformClient getOAuth2PlatformClient() {
        return factory.getOAuth2PlatformClient();
    }

    public OAuth2Config getOAuth2Config() {
        return factory.getOAuth2Config();
    }

    private class OAuth2PlatformClientFactory {

        private final QuickBooksConfig quickBooksConfig;

        private final OAuth2PlatformClient client;
        private final OAuth2Config oauth2Config;

        private OAuth2PlatformClientFactory(QuickBooksConfig quickBooksConfig) {
            this.quickBooksConfig = quickBooksConfig;

            //initialize the config
            oauth2Config = new OAuth2Config.OAuth2ConfigBuilder(quickBooksConfig.getOAuth2AppClientId(), quickBooksConfig.getOAuth2AppClientSecret()) //set client id, secret
                    .callDiscoveryAPI(envConfig.isProduction() ? Environment.PRODUCTION : Environment.SANDBOX) // call discovery API to populate urls
                    .buildConfig();
            //build the client
            client = new OAuth2PlatformClient(oauth2Config);
        }

        private OAuth2PlatformClient getOAuth2PlatformClient() {
            return client;
        }

        private OAuth2Config getOAuth2Config() {
            return oauth2Config;
        }

        private String getAccountingAPIHost() {
            return quickBooksConfig.getIntuitAccountingAPIHost();
        }

    }

    public class InterceptingDataService extends DataService {

        private QuickBooksAction<?> action = null;

        public InterceptingDataService(Context context) {
            super(context);
        }

        public InterceptingDataService(Context context, QuickBooksAction<?> action) {
            super(context);
            this.action = action;
        }

        @Override
        public void executeQueryAsync(String query, CallbackHandler callbackHandler) throws FMSException {
            //If not null, then this is the first attempt, so if we get an auth error, refresh tokens and try again
            if (action != null) {
                    super.executeQueryAsync(query, callbackMessage -> executeIntercept(callbackHandler, callbackMessage));
            } else {
                super.executeQueryAsync(query, callbackHandler);
            }
        }

        @Override
        public void executeBatchAsync(BatchOperation batchOperation, CallbackHandler callbackHandler) throws FMSException {
            if (action != null) {
                super.executeBatchAsync(batchOperation, callbackMessage -> executeIntercept(callbackHandler, callbackMessage));
            } else {
                super.executeBatchAsync(batchOperation, callbackHandler);
            }

        }

        public void setAction(QuickBooksAction<?> action) {
            this.action = action;
        }

        private void executeIntercept(CallbackHandler handler, CallbackMessage message) {
            if (message.getFMSException() != null) {
                //TODO Check for an actual 401
                refreshTokens(getSetting(KEY_REALM_ID), action);
            } else {
                //No issue occurred
                handler.execute(message);
            }
        }

    }


}
