/*
 * Copyright (c) 2012-2016, b3log.org & hacpai.com & fangstar.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.b3log.symphony.processor;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.Pagination;
import org.b3log.latke.model.User;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.servlet.annotation.After;
import org.b3log.latke.servlet.annotation.Before;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.servlet.renderer.freemarker.AbstractFreeMarkerRenderer;
import org.b3log.latke.util.CollectionUtils;
import org.b3log.latke.util.MD5;
import org.b3log.latke.util.Strings;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Comment;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.Notification;
import org.b3log.symphony.model.Option;
import org.b3log.symphony.model.Order;
import org.b3log.symphony.model.Pointtransfer;
import org.b3log.symphony.model.Product;
import org.b3log.symphony.model.Tag;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.processor.advice.AdminCheck;
import org.b3log.symphony.processor.advice.CSRFCheck;
import org.b3log.symphony.processor.advice.CSRFToken;
import org.b3log.symphony.processor.advice.MallAdminCheck;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchEndAdvice;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchStartAdvice;
import org.b3log.symphony.processor.advice.validate.UserRegister2Validation;
import org.b3log.symphony.processor.advice.validate.UserRegisterValidation;
import org.b3log.symphony.service.ArticleMgmtService;
import org.b3log.symphony.service.ArticleQueryService;
import org.b3log.symphony.service.CommentMgmtService;
import org.b3log.symphony.service.CommentQueryService;
import org.b3log.symphony.service.NotificationMgmtService;
import org.b3log.symphony.service.OptionMgmtService;
import org.b3log.symphony.service.OptionQueryService;
import org.b3log.symphony.service.OrderMgmtService;
import org.b3log.symphony.service.OrderQueryService;
import org.b3log.symphony.service.PointtransferMgmtService;
import org.b3log.symphony.service.PointtransferQueryService;
import org.b3log.symphony.service.ProductMgmtService;
import org.b3log.symphony.service.ProductQueryService;
import org.b3log.symphony.service.SearchMgmtService;
import org.b3log.symphony.service.TagMgmtService;
import org.b3log.symphony.service.TagQueryService;
import org.b3log.symphony.service.UserMgmtService;
import org.b3log.symphony.service.UserQueryService;
import org.b3log.symphony.util.Filler;
import org.b3log.symphony.util.Mails;
import org.b3log.symphony.util.Symphonys;
import org.b3log.symphony.util.Times;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Admin processor.
 *
 * <ul>
 * <li>Shows admin index (/admin/index), GET</li>
 * <li>Shows users (/admin/users), GET</li>
 * <li>Shows a user (/admin/user/{userId}), GET</li>
 * <li>Shows add user (/admin/add-user), GET</li>
 * <li>Adds a user (/admin/add-user), POST</li>
 * <li>Updates a user (/admin/user/{userId}), POST</li>
 * <li>Updates a user's email (/admin/user/{userId}/email), POST</li>
 * <li>Updates a user's username (/admin/user/{userId}/username), POST</li>
 * <li>Charges a user's point (/admin/user/{userId}/charge-point), POST</li>
 * <li>Exchanges a user's point (/admin/user/{userId}/exchange-point), POST</li>
 * <li>Deducts a user's abuse point (/admin/user/{userId}/abuse-point), POST</li>
 * <li>Shows articles (/admin/articles), GET</li>
 * <li>Shows an article (/admin/article/{articleId}), GET</li>
 * <li>Updates an article (/admin/article/{articleId}), POST</li>
 * <li>Shows comments (/admin/comments), GET</li>
 * <li>Show a comment (/admin/comment/{commentId}), GET</li>
 * <li>Updates a comment (/admin/comment/{commentId}), POST</li>
 * <li>Shows tags (/admin/tags), GET</li>
 * <li>Show a tag (/admin/tag/{tagId}), GET</li>
 * <li>Updates a tag (/admin/tag/{tagId}), POST</li>
 * <li>Shows products (/admin/products), GET</li>
 * <li>Shows add product (/admin/add-product), GET</li>
 * <li>Adds a product (/admin/add-product), POST</li>
 * <li>Shows a product (/admin/product/{productId}), GET</li>
 * <li>Updates a product (/admin/product/{productId}), POST</li>
 * <li>Shows orders (/admin/orders), GET</li>
 * <li>Confirms an order (/admin/order/{orderId}/confirm)</li>
 * <li>Refunds an order (/admin/order/{orderId}/refund)</li>
 * <li>Shows point charge records (/admin/charge-records), GET</li>
 * <li>Shows miscellaneous (/admin/misc), GET</li>
 * <li>Updates miscellaneous (/admin/misc), POST</li>
 * <li>Search index (/admin/search/index), POST</li>
 * </ul>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 2.14.3.2, Oct 11, 2016
 * @since 1.1.0
 */
@RequestProcessor
public class AdminProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(AdminProcessor.class.getName());

    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

    /**
     * User query service.
     */
    @Inject
    private UserQueryService userQueryService;

    /**
     * User management service.
     */
    @Inject
    private UserMgmtService userMgmtService;

    /**
     * Article query service.
     */
    @Inject
    private ArticleQueryService articleQueryService;

    /**
     * Article management service.
     */
    @Inject
    private ArticleMgmtService articleMgmtService;

    /**
     * Comment query service.
     */
    @Inject
    private CommentQueryService commentQueryService;

    /**
     * Comment management service.
     */
    @Inject
    private CommentMgmtService commentMgmtService;

    /**
     * Option query service.
     */
    @Inject
    private OptionQueryService optionQueryService;

    /**
     * Option management service.
     */
    @Inject
    private OptionMgmtService optionMgmtService;

    /**
     * Tag query service.
     */
    @Inject
    private TagQueryService tagQueryService;

    /**
     * Tag management service.
     */
    @Inject
    private TagMgmtService tagMgmtService;

    /**
     * Product query service.
     */
    @Inject
    private ProductQueryService productQueryService;

    /**
     * Product management service.
     */
    @Inject
    private ProductMgmtService productMgmtService;

    /**
     * Order query service.
     */
    @Inject
    private OrderQueryService orderQueryService;

    /**
     * Order management service.
     */
    @Inject
    private OrderMgmtService orderMgmtService;

    /**
     * Pointtransfer management service.
     */
    @Inject
    private PointtransferMgmtService pointtransferMgmtService;

    /**
     * Pointtransfer query service.
     */
    @Inject
    private PointtransferQueryService pointtransferQueryService;

    /**
     * Notification management service.
     */
    @Inject
    private NotificationMgmtService notificationMgmtService;

    /**
     * Search management service.
     */
    @Inject
    private SearchMgmtService searchMgmtService;

    /**
     * Filler.
     */
    @Inject
    private Filler filler;

    /**
     * Shows admin index.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void showIndex(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("admin/index.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        filler.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Shows admin users.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/users", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void showUsers(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("admin/users.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        String pageNumStr = request.getParameter("p");
        if (Strings.isEmptyOrNull(pageNumStr) || !Strings.isNumeric(pageNumStr)) {
            pageNumStr = "1";
        }

        final int pageNum = Integer.valueOf(pageNumStr);
        final int pageSize = Symphonys.PAGE_SIZE;
        final int windowSize = Symphonys.WINDOW_SIZE;

        final JSONObject requestJSONObject = new JSONObject();
        requestJSONObject.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        requestJSONObject.put(Pagination.PAGINATION_PAGE_SIZE, pageSize);
        requestJSONObject.put(Pagination.PAGINATION_WINDOW_SIZE, windowSize);

        final String nameOrEmail = request.getParameter(Common.USER_NAME_OR_EMAIL);
        if (!Strings.isEmptyOrNull(nameOrEmail)) {
            requestJSONObject.put(Common.USER_NAME_OR_EMAIL, nameOrEmail);
        }

        final JSONObject result = userQueryService.getUsers(requestJSONObject);

        dataModel.put(User.USERS, CollectionUtils.jsonArrayToList(result.optJSONArray(User.USERS)));

        final JSONObject pagination = result.optJSONObject(Pagination.PAGINATION);
        final int pageCount = pagination.optInt(Pagination.PAGINATION_PAGE_COUNT);
        final JSONArray pageNums = pagination.optJSONArray(Pagination.PAGINATION_PAGE_NUMS);
        dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.opt(0));
        dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.opt(pageNums.length() - 1));
        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, CollectionUtils.jsonArrayToList(pageNums));

        filler.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Shows a user.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @param userId the specified user id
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/user/{userId}", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void showUser(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
            final String userId) throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("admin/user.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final JSONObject user = userQueryService.getUser(userId);
        dataModel.put(User.USER, user);

        final String teamsStr = Symphonys.get("teams");
        dataModel.put(Common.TEAMS, teamsStr.split(","));

        filler.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Shows add user.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/add-user", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void showAddUser(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("admin/add-user.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final String teamsStr = Symphonys.get("teams");
        dataModel.put(Common.TEAMS, teamsStr.split(","));

        dataModel.put(User.USER_PASSWORD, RandomStringUtils.randomAlphanumeric(6));

        filler.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Adds a user.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/add-user", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void addUser(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final String userName = request.getParameter(User.USER_NAME);
        final String userRealName = request.getParameter(UserExt.USER_REAL_NAME);
        final String email = request.getParameter(User.USER_EMAIL);
        final String password = request.getParameter(User.USER_PASSWORD);
        final String team = request.getParameter(UserExt.USER_TEAM);

        final boolean nameInvalid = UserRegisterValidation.invalidUserName(userName);
        final boolean emailInvalid = !Strings.isEmail(email);
        final boolean passwordInvalid = UserRegister2Validation.invalidUserPassword(password);

        if (nameInvalid || emailInvalid || passwordInvalid) {
            final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
            context.setRenderer(renderer);
            renderer.setTemplateName("admin/error.ftl");
            final Map<String, Object> dataModel = renderer.getDataModel();

            if (nameInvalid) {
                dataModel.put(Keys.MSG, langPropsService.get("invalidUserNameLabel"));
            } else if (emailInvalid) {
                dataModel.put(Keys.MSG, langPropsService.get("invalidEmailLabel"));
            } else if (passwordInvalid) {
                dataModel.put(Keys.MSG, langPropsService.get("invalidPasswordLabel"));
            }

            filler.fillHeaderAndFooter(request, response, dataModel);

            return;
        }

        String userId;
        try {
            final JSONObject user = new JSONObject();
            user.put(User.USER_NAME, userName);
            user.put(UserExt.USER_REAL_NAME, userRealName);
            user.put(User.USER_EMAIL, email);
            user.put(User.USER_PASSWORD, MD5.hash(password));
            user.put(UserExt.USER_APP_ROLE, UserExt.USER_APP_ROLE_C_HACKER);
            user.put(UserExt.USER_TEAM, team);
            user.put(UserExt.USER_STATUS, UserExt.USER_STATUS_C_VALID);

            userId = userMgmtService.addUser(user);

            if (!Symphonys.getBoolean("sendcloud.enabled")) {
                String body = langPropsService.get("accountCreatedBodyLabel");
                body = body.replace("${userName}", userName).replace("${password}", password).
                        replace("${servePath}", Latkes.getServePath());
                Mails.send(email, langPropsService.get("accountCreatedSubjectLabel"), body);
            }
        } catch (final Exception e) {
            final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
            context.setRenderer(renderer);
            renderer.setTemplateName("admin/error.ftl");
            final Map<String, Object> dataModel = renderer.getDataModel();

            dataModel.put(Keys.MSG, e.getMessage());
            filler.fillHeaderAndFooter(request, response, dataModel);

            return;
        }

        response.sendRedirect(Latkes.getServePath() + "/admin/user/" + userId);
    }

    /**
     * Updates a user.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @param userId the specified user id
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/user/{userId}", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void updateUser(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
            final String userId) throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("admin/user.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final JSONObject user = userQueryService.getUser(userId);
        dataModel.put(User.USER, user);

        final Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            final String name = parameterNames.nextElement();
            final String value = request.getParameter(name);

            if (name.equals(UserExt.USER_POINT) || name.equals(UserExt.USER_STATUS)) {
                user.put(name, Integer.valueOf(value));
            } else if (name.equals(User.USER_PASSWORD)) {
                final String oldPwd = (String) user.getString(name);
                if (!oldPwd.equals(value) && !Strings.isEmptyOrNull(value)) {
                    user.put(name, MD5.hash(value));
                }
            } else {
                user.put(name, value);
            }
        }

        userMgmtService.updateUser(userId, user);

        final String teamsStr = Symphonys.get("teams");
        dataModel.put(Common.TEAMS, teamsStr.split(","));

        filler.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Updates a user's email.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @param userId the specified user id
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/user/{userId}/email", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void updateUserEmail(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
            final String userId) throws Exception {
        final JSONObject user = userQueryService.getUser(userId);
        final String oldEmail = user.optString(User.USER_EMAIL);
        final String newEmail = request.getParameter(User.USER_EMAIL);

        if (oldEmail.equals(newEmail)) {
            response.sendRedirect(Latkes.getServePath() + "/admin/user/" + userId);

            return;
        }

        user.put(User.USER_EMAIL, newEmail);

        try {
            userMgmtService.updateUserEmail(userId, user);
        } catch (final ServiceException e) {
            final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
            context.setRenderer(renderer);
            renderer.setTemplateName("admin/error.ftl");
            final Map<String, Object> dataModel = renderer.getDataModel();

            dataModel.put(Keys.MSG, e.getMessage());
            filler.fillHeaderAndFooter(request, response, dataModel);

            return;
        }

        response.sendRedirect(Latkes.getServePath() + "/admin/user/" + userId);
    }

    /**
     * Updates a user's username.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @param userId the specified user id
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/user/{userId}/username", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void updateUserName(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
            final String userId) throws Exception {
        final JSONObject user = userQueryService.getUser(userId);
        final String oldUserName = user.optString(User.USER_NAME);
        final String newUserName = request.getParameter(User.USER_NAME);

        if (oldUserName.equals(newUserName)) {
            response.sendRedirect(Latkes.getServePath() + "/admin/user/" + userId);

            return;
        }

        user.put(User.USER_NAME, newUserName);

        try {
            userMgmtService.updateUserName(userId, user);
        } catch (final ServiceException e) {
            final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
            context.setRenderer(renderer);
            renderer.setTemplateName("admin/error.ftl");
            final Map<String, Object> dataModel = renderer.getDataModel();

            dataModel.put(Keys.MSG, e.getMessage());
            filler.fillHeaderAndFooter(request, response, dataModel);

            return;
        }

        response.sendRedirect(Latkes.getServePath() + "/admin/user/" + userId);
    }

    /**
     * Charges a user's point.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/charge-point", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {StopwatchStartAdvice.class, MallAdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void chargePoint(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final String userName = request.getParameter(User.USER_NAME);
        final String pointStr = request.getParameter(Common.POINT);
        final String memo = request.getParameter(Common.MEMO);

        if (StringUtils.isBlank(pointStr) || !StringUtils.isNumeric(memo)) {
            final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
            context.setRenderer(renderer);
            renderer.setTemplateName("admin/error.ftl");
            final Map<String, Object> dataModel = renderer.getDataModel();

            dataModel.put(Keys.MSG, "Charge point memo format error");
            filler.fillHeaderAndFooter(request, response, dataModel);

            return;
        }

        final JSONObject user = userQueryService.getUserByName(userName);
        if (null == user) {
            final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
            context.setRenderer(renderer);
            renderer.setTemplateName("admin/error.ftl");
            final Map<String, Object> dataModel = renderer.getDataModel();

            dataModel.put(Keys.MSG, "User [name=" + userName + "] not found");
            filler.fillHeaderAndFooter(request, response, dataModel);

            return;
        }

        final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);

        try {
            final int point = Integer.valueOf(pointStr);

            final String transferId = pointtransferMgmtService.transfer(Pointtransfer.ID_C_SYS, user.optString(Keys.OBJECT_ID),
                    Pointtransfer.TRANSFER_TYPE_C_CHARGE, point, memo + "-" + currentUser.optString(Keys.OBJECT_ID));

            final JSONObject notification = new JSONObject();
            notification.put(Notification.NOTIFICATION_USER_ID, user.optString(Keys.OBJECT_ID));
            notification.put(Notification.NOTIFICATION_DATA_ID, transferId);

            notificationMgmtService.addPointChargeNotification(notification);
        } catch (final Exception e) {
            final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
            context.setRenderer(renderer);
            renderer.setTemplateName("admin/error.ftl");
            final Map<String, Object> dataModel = renderer.getDataModel();

            dataModel.put(Keys.MSG, e.getMessage());
            filler.fillHeaderAndFooter(request, response, dataModel);

            return;
        }

        response.sendRedirect(Latkes.getServePath() + "/admin/charge-records");
    }

    /**
     * Deducts a user's abuse point.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @param userId the specified user id
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/user/{userId}/abuse-point", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void abusePoint(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
            final String userId) throws Exception {
        final String pointStr = request.getParameter(Common.POINT);

        try {
            final int point = Integer.valueOf(pointStr);

            final JSONObject user = userQueryService.getUser(userId);
            final int currentPoint = user.optInt(UserExt.USER_POINT);

            if (currentPoint - point < 0) {
                final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
                context.setRenderer(renderer);
                renderer.setTemplateName("admin/error.ftl");
                final Map<String, Object> dataModel = renderer.getDataModel();

                dataModel.put(Keys.MSG, langPropsService.get("insufficientBalanceLabel"));
                filler.fillHeaderAndFooter(request, response, dataModel);

                return;
            }

            final String memo = request.getParameter(Common.MEMO);

            final String transferId = pointtransferMgmtService.transfer(userId, Pointtransfer.ID_C_SYS,
                    Pointtransfer.TRANSFER_TYPE_C_ABUSE_DEDUCT, point, memo);

            final JSONObject notification = new JSONObject();
            notification.put(Notification.NOTIFICATION_USER_ID, userId);
            notification.put(Notification.NOTIFICATION_DATA_ID, transferId);

            notificationMgmtService.addAbusePointDeductNotification(notification);
        } catch (final Exception e) {
            final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
            context.setRenderer(renderer);
            renderer.setTemplateName("admin/error.ftl");
            final Map<String, Object> dataModel = renderer.getDataModel();

            dataModel.put(Keys.MSG, e.getMessage());
            filler.fillHeaderAndFooter(request, response, dataModel);

            return;
        }

        response.sendRedirect(Latkes.getServePath() + "/admin/user/" + userId);
    }

    /**
     * Exchanges a user's point.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @param userId the specified user id
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/user/{userId}/exchange-point", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void exchangePoint(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
            final String userId) throws Exception {
        final String pointStr = request.getParameter(Common.POINT);

        try {
            final int point = Integer.valueOf(pointStr);

            final JSONObject user = userQueryService.getUser(userId);
            final int currentPoint = user.optInt(UserExt.USER_POINT);

            if (currentPoint - point < Symphonys.getInt("pointExchangeMin")) {
                final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
                context.setRenderer(renderer);
                renderer.setTemplateName("admin/error.ftl");
                final Map<String, Object> dataModel = renderer.getDataModel();

                dataModel.put(Keys.MSG, langPropsService.get("insufficientBalanceLabel"));
                filler.fillHeaderAndFooter(request, response, dataModel);

                return;
            }

            final String memo = String.valueOf(Math.floor(point / (double) Symphonys.getInt("pointExchangeUnit")));

            final String transferId = pointtransferMgmtService.transfer(userId, Pointtransfer.ID_C_SYS,
                    Pointtransfer.TRANSFER_TYPE_C_EXCHANGE, point, memo);

            final JSONObject notification = new JSONObject();
            notification.put(Notification.NOTIFICATION_USER_ID, userId);
            notification.put(Notification.NOTIFICATION_DATA_ID, transferId);

            notificationMgmtService.addPointExchangeNotification(notification);
        } catch (final Exception e) {
            final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
            context.setRenderer(renderer);
            renderer.setTemplateName("admin/error.ftl");
            final Map<String, Object> dataModel = renderer.getDataModel();

            dataModel.put(Keys.MSG, e.getMessage());
            filler.fillHeaderAndFooter(request, response, dataModel);

            return;
        }

        response.sendRedirect(Latkes.getServePath() + "/admin/user/" + userId);
    }

    /**
     * Shows admin articles.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/articles", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void showArticles(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("admin/articles.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        String pageNumStr = request.getParameter("p");
        if (Strings.isEmptyOrNull(pageNumStr) || !Strings.isNumeric(pageNumStr)) {
            pageNumStr = "1";
        }

        final int pageNum = Integer.valueOf(pageNumStr);
        final int pageSize = Symphonys.PAGE_SIZE;
        final int windowSize = Symphonys.WINDOW_SIZE;

        final JSONObject requestJSONObject = new JSONObject();
        requestJSONObject.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        requestJSONObject.put(Pagination.PAGINATION_PAGE_SIZE, pageSize);
        requestJSONObject.put(Pagination.PAGINATION_WINDOW_SIZE, windowSize);

        final String articleId = request.getParameter("id");
        if (!Strings.isEmptyOrNull(articleId)) {
            requestJSONObject.put(Keys.OBJECT_ID, articleId);
        }

        final Map<String, Class<?>> articleFields = new HashMap<String, Class<?>>();
        articleFields.put(Keys.OBJECT_ID, String.class);
        articleFields.put(Article.ARTICLE_TITLE, String.class);
        articleFields.put(Article.ARTICLE_PERMALINK, String.class);
        articleFields.put(Article.ARTICLE_CREATE_TIME, Long.class);
        articleFields.put(Article.ARTICLE_VIEW_CNT, Integer.class);
        articleFields.put(Article.ARTICLE_COMMENT_CNT, Integer.class);
        articleFields.put(Article.ARTICLE_AUTHOR_EMAIL, String.class);
        articleFields.put(Article.ARTICLE_AUTHOR_ID, String.class);
        articleFields.put(Article.ARTICLE_TAGS, String.class);
        articleFields.put(Article.ARTICLE_STATUS, Integer.class);

        final JSONObject result = articleQueryService.getArticles(requestJSONObject, articleFields);
        dataModel.put(Article.ARTICLES, CollectionUtils.jsonArrayToList(result.optJSONArray(Article.ARTICLES)));

        final JSONObject pagination = result.optJSONObject(Pagination.PAGINATION);
        final int pageCount = pagination.optInt(Pagination.PAGINATION_PAGE_COUNT);
        final JSONArray pageNums = pagination.optJSONArray(Pagination.PAGINATION_PAGE_NUMS);
        dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.opt(0));
        dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.opt(pageNums.length() - 1));
        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, CollectionUtils.jsonArrayToList(pageNums));

        filler.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Shows an article.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @param articleId the specified article id
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/article/{articleId}", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void showArticle(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
            final String articleId) throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("admin/article.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final JSONObject article = articleQueryService.getArticle(articleId);
        dataModel.put(Article.ARTICLE, article);

        filler.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Updates an article.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @param articleId the specified article id
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/article/{articleId}", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void updateArticle(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
            final String articleId) throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("admin/article.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        JSONObject article = articleQueryService.getArticle(articleId);

        final Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            final String name = parameterNames.nextElement();
            final String value = request.getParameter(name);

            article.put(name, value);
        }

        final String articleTags = articleMgmtService.formatArticleTags(article.optString(Article.ARTICLE_TAGS));
        article.put(Article.ARTICLE_TAGS, articleTags);

        articleMgmtService.updateArticle(articleId, article);

        article = articleQueryService.getArticle(articleId);
        dataModel.put(Article.ARTICLE, article);

        filler.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Shows admin comments.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/comments", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void showComments(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("admin/comments.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        String pageNumStr = request.getParameter("p");
        if (Strings.isEmptyOrNull(pageNumStr) || !Strings.isNumeric(pageNumStr)) {
            pageNumStr = "1";
        }

        final int pageNum = Integer.valueOf(pageNumStr);
        final int pageSize = Symphonys.PAGE_SIZE;
        final int windowSize = Symphonys.WINDOW_SIZE;

        final JSONObject requestJSONObject = new JSONObject();
        requestJSONObject.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        requestJSONObject.put(Pagination.PAGINATION_PAGE_SIZE, pageSize);
        requestJSONObject.put(Pagination.PAGINATION_WINDOW_SIZE, windowSize);

        final Map<String, Class<?>> commentFields = new HashMap<String, Class<?>>();
        commentFields.put(Keys.OBJECT_ID, String.class);
        commentFields.put(Comment.COMMENT_CREATE_TIME, String.class);
        commentFields.put(Comment.COMMENT_AUTHOR_ID, String.class);
        commentFields.put(Comment.COMMENT_ON_ARTICLE_ID, String.class);
        commentFields.put(Comment.COMMENT_SHARP_URL, String.class);
        commentFields.put(Comment.COMMENT_STATUS, Integer.class);
        commentFields.put(Comment.COMMENT_CONTENT, String.class);

        final JSONObject result = commentQueryService.getComments(requestJSONObject, commentFields);
        dataModel.put(Comment.COMMENTS, CollectionUtils.jsonArrayToList(result.optJSONArray(Comment.COMMENTS)));

        final JSONObject pagination = result.optJSONObject(Pagination.PAGINATION);
        final int pageCount = pagination.optInt(Pagination.PAGINATION_PAGE_COUNT);
        final JSONArray pageNums = pagination.optJSONArray(Pagination.PAGINATION_PAGE_NUMS);
        dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.opt(0));
        dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.opt(pageNums.length() - 1));
        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, CollectionUtils.jsonArrayToList(pageNums));

        filler.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Shows a comment.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @param commentId the specified comment id
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/comment/{commentId}", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void showComment(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
            final String commentId) throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("admin/comment.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final JSONObject comment = commentQueryService.getComment(commentId);
        dataModel.put(Comment.COMMENT, comment);

        filler.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Updates a comment.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @param commentId the specified comment id
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/comment/{commentId}", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void updateComment(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
            final String commentId) throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("admin/comment.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        JSONObject comment = commentQueryService.getComment(commentId);

        final Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            final String name = parameterNames.nextElement();
            final String value = request.getParameter(name);

            comment.put(name, value);
        }

        commentMgmtService.updateComment(commentId, comment);

        comment = commentQueryService.getComment(commentId);
        dataModel.put(Comment.COMMENT, comment);

        filler.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Shows admin miscellaneous.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/misc", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void showMisc(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("admin/misc.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final List<JSONObject> misc = optionQueryService.getMisc();
        dataModel.put(Option.OPTIONS, misc);

        filler.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Updates admin miscellaneous.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/misc", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void updateMisc(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("admin/misc.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        List<JSONObject> misc = new ArrayList<JSONObject>();

        final Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            final String name = parameterNames.nextElement();
            final String value = request.getParameter(name);

            final JSONObject option = new JSONObject();
            option.put(Keys.OBJECT_ID, name);
            option.put(Option.OPTION_VALUE, value);
            option.put(Option.OPTION_CATEGORY, Option.CATEGORY_C_MISC);

            misc.add(option);
        }

        for (final JSONObject option : misc) {
            optionMgmtService.updateOption(option.getString(Keys.OBJECT_ID), option);
        }

        misc = optionQueryService.getMisc();
        dataModel.put(Option.OPTIONS, misc);

        filler.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Shows admin tags.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/tags", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void showTags(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("admin/tags.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        String pageNumStr = request.getParameter("p");
        if (Strings.isEmptyOrNull(pageNumStr) || !Strings.isNumeric(pageNumStr)) {
            pageNumStr = "1";
        }

        final int pageNum = Integer.valueOf(pageNumStr);
        final int pageSize = Symphonys.PAGE_SIZE;
        final int windowSize = Symphonys.WINDOW_SIZE;

        final JSONObject requestJSONObject = new JSONObject();
        requestJSONObject.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        requestJSONObject.put(Pagination.PAGINATION_PAGE_SIZE, pageSize);
        requestJSONObject.put(Pagination.PAGINATION_WINDOW_SIZE, windowSize);

        final String tagTitle = request.getParameter(Common.TITLE);
        if (!Strings.isEmptyOrNull(tagTitle)) {
            requestJSONObject.put(Tag.TAG_TITLE, tagTitle);
        }

        final Map<String, Class<?>> tagFields = new HashMap<String, Class<?>>();
        tagFields.put(Keys.OBJECT_ID, String.class);
        tagFields.put(Tag.TAG_TITLE, String.class);
        tagFields.put(Tag.TAG_DESCRIPTION, String.class);
        tagFields.put(Tag.TAG_ICON_PATH, String.class);
        tagFields.put(Tag.TAG_COMMENT_CNT, Integer.class);
        tagFields.put(Tag.TAG_REFERENCE_CNT, Integer.class);
        tagFields.put(Tag.TAG_FOLLOWER_CNT, Integer.class);
        tagFields.put(Tag.TAG_STATUS, Integer.class);
        tagFields.put(Tag.TAG_GOOD_CNT, Integer.class);
        tagFields.put(Tag.TAG_BAD_CNT, Integer.class);

        final JSONObject result = tagQueryService.getTags(requestJSONObject, tagFields);
        dataModel.put(Tag.TAGS, CollectionUtils.jsonArrayToList(result.optJSONArray(Tag.TAGS)));

        final JSONObject pagination = result.optJSONObject(Pagination.PAGINATION);
        final int pageCount = pagination.optInt(Pagination.PAGINATION_PAGE_COUNT);
        final JSONArray pageNums = pagination.optJSONArray(Pagination.PAGINATION_PAGE_NUMS);
        dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.opt(0));
        dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.opt(pageNums.length() - 1));
        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, CollectionUtils.jsonArrayToList(pageNums));

        filler.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Shows a tag.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @param tagId the specified tag id
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/tag/{tagId}", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void showTag(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
            final String tagId) throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("admin/tag.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final JSONObject tag = tagQueryService.getTag(tagId);
        dataModel.put(Tag.TAG, tag);

        filler.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Updates a tag.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @param tagId the specified tag id
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/tag/{tagId}", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void updateTag(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
            final String tagId) throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("admin/tag.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        JSONObject tag = tagQueryService.getTag(tagId);

        final String oldTitle = tag.optString(Tag.TAG_TITLE);

        final Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            final String name = parameterNames.nextElement();
            final String value = request.getParameter(name);

            tag.put(name, value);
        }

        final String newTitle = tag.optString(Tag.TAG_TITLE);

        if (oldTitle.equalsIgnoreCase(newTitle)) {
            tagMgmtService.updateTag(tagId, tag);
        }

        tag = tagQueryService.getTag(tagId);
        dataModel.put(Tag.TAG, tag);

        filler.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Shows admin products.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/products", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, MallAdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void showProducts(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("admin/products.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        String pageNumStr = request.getParameter("p");
        if (Strings.isEmptyOrNull(pageNumStr) || !Strings.isNumeric(pageNumStr)) {
            pageNumStr = "1";
        }

        final int pageNum = Integer.valueOf(pageNumStr);
        final int pageSize = Symphonys.PAGE_SIZE;
        final int windowSize = Symphonys.WINDOW_SIZE;

        final JSONObject requestJSONObject = new JSONObject();
        requestJSONObject.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        requestJSONObject.put(Pagination.PAGINATION_PAGE_SIZE, pageSize);
        requestJSONObject.put(Pagination.PAGINATION_WINDOW_SIZE, windowSize);

        final String name = request.getParameter(Common.NAME);
        if (!Strings.isEmptyOrNull(name)) {
            requestJSONObject.put(Product.PRODUCT_NAME, name);
        }

        final Map<String, Class<?>> fields = new HashMap<String, Class<?>>();
        fields.put(Keys.OBJECT_ID, String.class);
        fields.put(Product.PRODUCT_CATEGORY, String.class);
        fields.put(Product.PRODUCT_DESCRIPTION, String.class);
        fields.put(Product.PRODUCT_NAME, String.class);
        fields.put(Product.PRODUCT_PRICE, Double.class);
        fields.put(Product.PRODUCT_IMG_URL, String.class);
        fields.put(Product.PRODUCT_COUNT, Integer.class);
        fields.put(Product.PRODUCT_STATUS, Integer.class);

        final JSONObject result = productQueryService.getProducts(requestJSONObject, fields);
        dataModel.put(Product.PRODUCTS, CollectionUtils.jsonArrayToList(result.optJSONArray(Product.PRODUCTS)));

        final JSONObject pagination = result.optJSONObject(Pagination.PAGINATION);
        final int pageCount = pagination.optInt(Pagination.PAGINATION_PAGE_COUNT);
        final JSONArray pageNums = pagination.optJSONArray(Pagination.PAGINATION_PAGE_NUMS);
        dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.opt(0));
        dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.opt(pageNums.length() - 1));
        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, CollectionUtils.jsonArrayToList(pageNums));

        filler.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Shows add product.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/add-product", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, MallAdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void showAddProduct(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("admin/add-product.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        filler.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Adds a product.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/add-product", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {StopwatchStartAdvice.class, MallAdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void addProduct(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final String category = request.getParameter(Product.PRODUCT_CATEGORY);
        final String description = request.getParameter(Product.PRODUCT_DESCRIPTION);
        final String name = request.getParameter(Product.PRODUCT_NAME);
        final String price = request.getParameter(Product.PRODUCT_PRICE);
        String imgURL = request.getParameter(Product.PRODUCT_IMG_URL);
        final String count = request.getParameter(Product.PRODUCT_COUNT);
        final String status = request.getParameter(Product.PRODUCT_STATUS);

        if (StringUtils.isBlank(imgURL)) {
            imgURL = "";
        }

        if (StringUtils.isBlank(category) || StringUtils.length(category) > 20) {
            final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
            context.setRenderer(renderer);
            renderer.setTemplateName("admin/error.ftl");
            final Map<String, Object> dataModel = renderer.getDataModel();

            dataModel.put(Keys.MSG, langPropsService.get("invalidProductCategoryLabel"));

            filler.fillHeaderAndFooter(request, response, dataModel);

            return;
        }

        try {
            final JSONObject product = new JSONObject();
            product.put(Product.PRODUCT_CATEGORY, category);
            product.put(Product.PRODUCT_DESCRIPTION, description);
            product.put(Product.PRODUCT_NAME, name);
            product.put(Product.PRODUCT_PRICE, price);
            product.put(Product.PRODUCT_IMG_URL, imgURL);
            product.put(Product.PRODUCT_COUNT, count);
            product.put(Product.PRODUCT_STATUS, status);

            productMgmtService.addProduct(product);
        } catch (final Exception e) {
            final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
            context.setRenderer(renderer);
            renderer.setTemplateName("admin/error.ftl");
            final Map<String, Object> dataModel = renderer.getDataModel();

            dataModel.put(Keys.MSG, e.getMessage());
            filler.fillHeaderAndFooter(request, response, dataModel);

            return;
        }

        response.sendRedirect(Latkes.getServePath() + "/admin/products");
    }

    /**
     * Shows a product.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @param productId the specified product id
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/product/{productId}", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, MallAdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void showProduct(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
            final String productId) throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("admin/product.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final JSONObject product = productQueryService.getProduct(productId);
        dataModel.put(Product.PRODUCT, product);

        filler.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Updates a product.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @param productId the specified product id
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/product/{productId}", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {StopwatchStartAdvice.class, MallAdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void updateProduct(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
            final String productId) throws Exception {
        final JSONObject product = productQueryService.getProduct(productId);

        final Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            final String name = parameterNames.nextElement();
            final String value = request.getParameter(name);

            product.put(name, value);

            if (name.equals(Product.PRODUCT_STATUS) || name.equals(Product.PRODUCT_COUNT)) {
                product.put(name, Integer.valueOf(value));
            }

            if (name.equals(Product.PRODUCT_CATEGORY)) {
                final String category = value;
                if (StringUtils.isBlank(category) || StringUtils.length(category) > 20) {
                    final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
                    context.setRenderer(renderer);
                    renderer.setTemplateName("admin/error.ftl");
                    final Map<String, Object> dataModel = renderer.getDataModel();

                    dataModel.put(Keys.MSG, langPropsService.get("invalidProductCategoryLabel"));

                    filler.fillHeaderAndFooter(request, response, dataModel);

                    return;
                }
            }
        }

        productMgmtService.updateProduct(product);

        response.sendRedirect(Latkes.getServePath() + "/admin/products");
    }

    /**
     * Shows admin orders.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/orders", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, MallAdminCheck.class})
    @After(adviceClass = {CSRFToken.class, StopwatchEndAdvice.class})
    public void showOrders(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("admin/orders.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        String pageNumStr = request.getParameter("p");
        if (Strings.isEmptyOrNull(pageNumStr) || !Strings.isNumeric(pageNumStr)) {
            pageNumStr = "1";
        }

        final int pageNum = Integer.valueOf(pageNumStr);
        final int pageSize = Symphonys.PAGE_SIZE;
        final int windowSize = Symphonys.WINDOW_SIZE;

        final JSONObject requestJSONObject = new JSONObject();
        requestJSONObject.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        requestJSONObject.put(Pagination.PAGINATION_PAGE_SIZE, pageSize);
        requestJSONObject.put(Pagination.PAGINATION_WINDOW_SIZE, windowSize);

        final String category = request.getParameter(Common.CATEGORY);
        if (!Strings.isEmptyOrNull(category)) {
            requestJSONObject.put(Order.ORDER_PRODUCT_CATEGORY, category);
            dataModel.put(Common.CATEGORY, category);
        } else {
            dataModel.put(Common.CATEGORY, "");
        }

        final String status = request.getParameter(Common.STATUS);
        if (!Strings.isEmptyOrNull(status)) {
            requestJSONObject.put(Order.ORDER_STATUS, status);
            dataModel.put(Common.STATUS, status);
        } else {
            requestJSONObject.put(Order.ORDER_STATUS, Order.ORDER_STATUS_C_INIT);
            dataModel.put(Common.STATUS, String.valueOf(Order.ORDER_STATUS_C_INIT));
        }

        final String from = request.getParameter(Common.FROM);
        if (!Strings.isEmptyOrNull(from)) {
            final Date date = DateUtils.parseDate(from, new String[]{"yyyy-MM-dd"});
            requestJSONObject.put(Common.FROM, date.getTime());
            dataModel.put(Common.FROM, DateFormatUtils.format(date, "yyyy-MM-dd"));
        } else {
            final Date date = DateUtils.addMonths(new Date(), -1);
            requestJSONObject.put(Common.FROM, date.getTime());
            dataModel.put(Common.FROM, DateFormatUtils.format(date, "yyyy-MM-dd"));
        }

        final String to = request.getParameter(Common.TO);
        if (!Strings.isEmptyOrNull(to)) {
            final Date date = DateUtils.parseDate(to, new String[]{"yyyy-MM-dd"});
            requestJSONObject.put(Common.TO, Times.getDayEndTime(date.getTime()));
            dataModel.put(Common.TO, DateFormatUtils.format(date, "yyyy-MM-dd"));
        } else {
            requestJSONObject.put(Common.TO, Times.getDayEndTime(System.currentTimeMillis()));
            dataModel.put(Common.TO, DateFormatUtils.format(new Date(), "yyyy-MM-dd"));
        }

        final Map<String, Class<?>> fields = new HashMap<String, Class<?>>();
        fields.put(Keys.OBJECT_ID, String.class);
        fields.put(Order.ORDER_CONFIRM_TIME, Long.class);
        fields.put(Order.ORDER_CREATE_TIME, Long.class);
        fields.put(Order.ORDER_HANDLER_ID, String.class);
        fields.put(Order.ORDER_POINT, Integer.class);
        fields.put(Order.ORDER_PRICE, Double.class);
        fields.put(Order.ORDER_PRODUCT_NAME, String.class);
        fields.put(Order.ORDER_STATUS, Integer.class);
        fields.put(Order.ORDER_BUYER_ID, String.class);

        final JSONObject result = orderQueryService.getOrders(requestJSONObject, fields);
        dataModel.put(Order.ORDERS, CollectionUtils.jsonArrayToList(result.optJSONArray(Order.ORDERS)));

        final JSONObject pagination = result.optJSONObject(Pagination.PAGINATION);
        final int pageCount = pagination.optInt(Pagination.PAGINATION_PAGE_COUNT);
        final JSONArray pageNums = pagination.optJSONArray(Pagination.PAGINATION_PAGE_NUMS);
        dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.opt(0));
        dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.opt(pageNums.length() - 1));
        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, CollectionUtils.jsonArrayToList(pageNums));

        filler.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Confirms an order.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @param orderId the specified order id
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/order/{orderId}/confirm", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {StopwatchStartAdvice.class, MallAdminCheck.class, CSRFCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void confirmOrder(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
            final String orderId) throws Exception {
        context.renderJSON().renderFalseResult();

        final JSONObject order = orderQueryService.getOrder(orderId);
        if (null == order) {
            context.renderMsg("Order not found");

            return;
        }

        if (Order.ORDER_STATUS_C_INIT != order.optInt(Order.ORDER_STATUS)) {
            context.renderMsg("Order has been handled");

            return;
        }

        final JSONObject handler = (JSONObject) request.getAttribute(User.USER);

        order.put(Order.ORDER_CONFIRM_TIME, System.currentTimeMillis());
        order.put(Order.ORDER_HANDLER_ID, handler.optString(Keys.OBJECT_ID));
        order.put(Order.ORDER_STATUS, Order.ORDER_STATUS_C_CONFIRMED);

        orderMgmtService.updateOrder(order);

        context.renderTrueResult().renderMsg(langPropsService.get("confirmSuccLabel"));
    }

    /**
     * Refunds an order.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @param orderId the specified order id
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/order/{orderId}/refund", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {StopwatchStartAdvice.class, MallAdminCheck.class, CSRFCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void refundOrder(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
            final String orderId) throws Exception {
        context.renderJSON().renderFalseResult();

        final JSONObject order = orderQueryService.getOrder(orderId);
        if (null == order) {
            context.renderMsg("Order not found");

            return;
        }

        if (Order.ORDER_STATUS_C_INIT != order.optInt(Order.ORDER_STATUS)) {
            context.renderMsg("Order has been handled");

            return;
        }

        final JSONObject handler = (JSONObject) request.getAttribute(User.USER);

        order.put(Order.ORDER_CONFIRM_TIME, System.currentTimeMillis());
        order.put(Order.ORDER_HANDLER_ID, handler.optString(Keys.OBJECT_ID));
        order.put(Order.ORDER_STATUS, Order.ORDER_STATUS_C_REFUNDED);

        orderMgmtService.updateOrder(order);

        final String buyerId = order.optString(Order.ORDER_BUYER_ID);
        final int point = order.optInt(Order.ORDER_POINT);

        pointtransferMgmtService.transfer("sys", buyerId, Pointtransfer.TRANSFER_TYPE_C_REFUND_PRODUCT, point, orderId);

        context.renderTrueResult().renderMsg(langPropsService.get("refundSuccLabel"));
    }

    /**
     * Shows admin point charge records.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/charge-records", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, MallAdminCheck.class})
    @After(adviceClass = {CSRFToken.class, StopwatchEndAdvice.class})
    public void showChargeRecords(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("admin/charge-records.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        String pageNumStr = request.getParameter("p");
        if (Strings.isEmptyOrNull(pageNumStr) || !Strings.isNumeric(pageNumStr)) {
            pageNumStr = "1";
        }

        final int pageNum = Integer.valueOf(pageNumStr);
        final int pageSize = Symphonys.PAGE_SIZE;

        final JSONObject result = pointtransferQueryService.getChargeRecords(pageNum, pageSize);
        final List<JSONObject> results = (List<JSONObject>) result.opt(Keys.RESULTS);
        for (final JSONObject record : results) {
            final String toUserId = record.optString(Pointtransfer.TO_ID);
            final JSONObject toUser = userQueryService.getUser(toUserId);
            record.put(User.USER_NAME, toUser.optString(User.USER_NAME));
            record.put(UserExt.USER_REAL_NAME, toUser.optString(UserExt.USER_REAL_NAME));

            final String handlerId = StringUtils.substringAfterLast(record.optString(Pointtransfer.DATA_ID), "-");
            final JSONObject handler = userQueryService.getUser(handlerId);
            record.put(Common.HANDLER_NAME, handler.optString(User.USER_NAME));
            record.put(Common.HANDLER_REAL_NAME, handler.optString(UserExt.USER_REAL_NAME));

            record.put(Pointtransfer.TIME, new Date(record.optLong(Pointtransfer.TIME)));
            record.put(Common.MONEY, StringUtils.substringBefore(record.optString(Pointtransfer.DATA_ID), "-"));
        }

        dataModel.put(Keys.RESULTS, results);

        final long chargePointSum = pointtransferQueryService.getChargePointSum();
        final int pointExchangeUnit = Symphonys.getInt("pointExchangeUnit");
        dataModel.put(Common.CHARGE_SUM, chargePointSum / pointExchangeUnit);

        final JSONObject pagination = result.optJSONObject(Pagination.PAGINATION);
        final int pageCount = pagination.optInt(Pagination.PAGINATION_PAGE_COUNT);
        final JSONArray pageNums = pagination.optJSONArray(Pagination.PAGINATION_PAGE_NUMS);
        dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.opt(0));
        dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.opt(pageNums.length() - 1));
        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, CollectionUtils.jsonArrayToList(pageNums));

        filler.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Shows admin point charge.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/point-charge", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, MallAdminCheck.class})
    @After(adviceClass = {CSRFToken.class, StopwatchEndAdvice.class})
    public void showPointCharge(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("admin/point-charge.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        filler.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Search index.
     *
     * @param context the specified context
     * @throws Exception exception
     */
    @RequestProcessing(value = "/admin/search/index", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {StopwatchStartAdvice.class, AdminCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void searchIndex(final HTTPRequestContext context) throws Exception {
        context.renderJSON(true);

        final JSONObject stat = optionQueryService.getStatistic();
        final int articleCount = stat.optInt(Option.ID_C_STATISTIC_ARTICLE_COUNT);

        final int pages = (int) Math.ceil((double) articleCount / 50.0);

        for (int pageNum = 1; pageNum <= pages; pageNum++) {
            final List<JSONObject> articles = articleQueryService.getArticles(pageNum, 50);

            for (final JSONObject article : articles) {
                final int articleType = article.optInt(Article.ARTICLE_TYPE);
                if (Article.ARTICLE_TYPE_C_DISCUSSION == articleType
                        || Article.ARTICLE_TYPE_C_THOUGHT == articleType) {
                    continue;
                }

                searchMgmtService.updateDocument(article, Article.ARTICLE);
            }

            LOGGER.info("Indexed page [" + pageNum + "]");
        }
    }
}
