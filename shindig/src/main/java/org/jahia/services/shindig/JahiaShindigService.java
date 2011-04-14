/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.shindig;

import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.common.util.ImmediateFuture;
import org.apache.shindig.protocol.DataCollection;
import org.apache.shindig.protocol.ProtocolException;
import org.apache.shindig.protocol.RestfulCollection;
import org.apache.shindig.protocol.model.SortOrder;
import org.apache.shindig.social.opensocial.model.Activity;
import org.apache.shindig.social.opensocial.model.Message;
import org.apache.shindig.social.opensocial.model.MessageCollection;
import org.apache.shindig.social.opensocial.model.Person;
import org.apache.shindig.social.opensocial.spi.*;
import org.jahia.api.Constants;
import org.jahia.modules.social.SocialService;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.services.usermanager.jcr.JCRUser;

import javax.jcr.*;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.concurrent.Future;

/**
 * Implementation of the Shindig person service, mapping Shindig people to Jahia users.
 *
 * @author loom
 *         Date: Aug 18, 2009
 *         Time: 8:04:20 AM
 */
public class JahiaShindigService implements PersonService, ActivityService, AppDataService, MessageService {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(JahiaShindigService.class);

    private static final Comparator<Person> NAME_COMPARATOR = new Comparator<Person>() {
        public int compare(Person person, Person person1) {
            String name = person.getName().getFormatted();
            String name1 = person1.getName().getFormatted();
            return name.compareTo(name1);
        }
    };

    private JahiaUserManagerService jahiaUserManagerService;
    private JCRTemplate jcrTemplate;

    public JahiaShindigService(JahiaUserManagerService jahiaUserManagerService, JCRTemplate jcrTemplate) {
        this.jahiaUserManagerService = jahiaUserManagerService;
        this.jcrTemplate = jcrTemplate;
    }

    /* -- PERSON SERVICE IMPLEMENTATION -- */

    /**
     * Returns a list of people that correspond to the passed in person ids.
     *
     * @param userIds           A set of users
     * @param groupId           The group
     * @param collectionOptions How to filter, sort and paginate the collection being fetched
     * @param fields            The profile details to fetch. Empty set implies all
     * @param token             The gadget token @return a list of people.
     */
    public Future<RestfulCollection<Person>> getPeople(Set<UserId> userIds, GroupId groupId, CollectionOptions collectionOptions, Set<String> fields, SecurityToken token) throws ProtocolException {
        try {
            List<Person> result = Lists.newArrayList();

            Set<String> idSet = getIdSet(userIds, groupId, token);

            for (String id : idSet) {
                result.add(loadPerson(id, fields));
            }

            if (GroupId.Type.self == groupId.getType() && result.isEmpty()) {
                throw new ProtocolException(HttpServletResponse.SC_BAD_REQUEST, "Person not found");
            }

            // We can pretend that by default the people are in top friends order
            if (collectionOptions.getSortBy().equals(Person.Field.NAME.toString())) {
                Collections.sort(result, NAME_COMPARATOR);
            }

            if (collectionOptions.getSortOrder() == SortOrder.descending) {
                Collections.reverse(result);
            }

            // TODO: The samplecontainer doesn't really have the concept of HAS_APP so
            // we can't support any filters yet. We should fix this.

            int totalSize = result.size();
            int last = collectionOptions.getFirst() + collectionOptions.getMax();
            result = result.subList(collectionOptions.getFirst(), Math.min(last, totalSize));

            return ImmediateFuture.newInstance(new RestfulCollection<Person>(result, collectionOptions.getFirst(),
                    totalSize));
        } catch (RepositoryException re) {
            throw new ProtocolException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, re.getMessage(), re);
        }
    }

    /**
     * Returns a person that corresponds to the passed in person id.
     *
     * @param id     The id of the person to fetch.
     * @param fields The fields to fetch.
     * @param token  The gadget token
     * @return a list of people.
     */
    public Future<Person> getPerson(UserId id, Set<String> fields, SecurityToken token) throws ProtocolException {
        String userKey = id.getUserId(token);
        return ImmediateFuture.newInstance(loadPerson(userKey, fields));
    }

    private Person loadPerson(String userKey, Set<String> fields) {
        try {
            JahiaUser jahiaUser = jahiaUserManagerService.lookupUserByKey(userKey);
            if (jahiaUser == null) {
                return null;
            }
            // we must now load all the custom properties from the JCR, if they exist
            JahiaPersonImpl jahiaPerson = loadPersonPropertiesFromJCR(jahiaUser);
            Map<String, Object> appData = getPersonAppData(userKey, fields);
            jahiaPerson.setAppData(appData);

            return jahiaPerson;
        } catch (RepositoryException re) {
            throw new ProtocolException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, re.getMessage(), re);
        }
    }

    /* -- ACTIVITY SERVICE IMPLEMENTATION -- */

    public Future<RestfulCollection<Activity>> getActivities(Set<UserId> userIds, GroupId groupId, final String appId, Set<String> fields, CollectionOptions options, SecurityToken token) throws ProtocolException {
        final List<Activity> result = Lists.newArrayList();
        try {
            Set<String> idSet = getIdSet(userIds, groupId, token);
            for (final String id : idSet) {

                jcrTemplate.doExecuteWithSystemSession(new JCRCallback<Object>() {
                    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {

                        Node userNode = getUsersNode(session, id);
                        // now let's retrieve the activities for the user.
                        Query myConnectionActivitiesQuery = session.getWorkspace().getQueryManager().createQuery("select * from [" + SocialService.JNT_SOCIAL_ACTIVITY + "] as uA where isdescendantnode(uA,['" + userNode.getPath() + "']) order by [jcr:created] desc", Query.JCR_SQL2);
                        myConnectionActivitiesQuery.setLimit(100);
                        QueryResult myConnectionActivitiesResult = myConnectionActivitiesQuery.execute();

                        NodeIterator myConnectionActivitiesIterator = myConnectionActivitiesResult.getNodes();
                        while (myConnectionActivitiesIterator.hasNext()) {
                            JCRNodeWrapper currentActivityNode = ((JCRNodeWrapper) myConnectionActivitiesIterator.nextNode());
                            JahiaActivityImpl activityImpl = new JahiaActivityImpl(currentActivityNode);
                            if (appId == null || (activityImpl.getAppId() == null)) {
                                result.add(new JahiaActivityImpl(currentActivityNode));
                            } else if (activityImpl.getAppId().equals(appId)) {
                                result.add(new JahiaActivityImpl(currentActivityNode));
                            }
                        }
                        return null;
                    }
                });
            }
            return ImmediateFuture.newInstance(new RestfulCollection<Activity>(result));
        } catch (RepositoryException re) {
            throw new ProtocolException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, re.getMessage(),
                    re);
        }
    }

    public Future<RestfulCollection<Activity>> getActivities(UserId userId, GroupId groupId, final String appId, Set<String> fields, CollectionOptions options, Set<String> activityIds, SecurityToken token) throws ProtocolException {
        final List<Activity> result = Lists.newArrayList();
        try {
            final String userKey = userId.getUserId(token);

            jcrTemplate.doExecuteWithSystemSession(new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {

                    Node userNode = getUsersNode(session, userKey);
                    // now let's retrieve the activities for the user.
                    Query myConnectionActivitiesQuery = session.getWorkspace().getQueryManager().createQuery("select * from [" + SocialService.JNT_SOCIAL_ACTIVITY + "] as uA where isdescendantnode(uA,['" + userNode.getPath() + "']) order by [jcr:created] desc", Query.JCR_SQL2);
                    myConnectionActivitiesQuery.setLimit(100);
                    QueryResult myConnectionActivitiesResult = myConnectionActivitiesQuery.execute();

                    NodeIterator myConnectionActivitiesIterator = myConnectionActivitiesResult.getNodes();
                    while (myConnectionActivitiesIterator.hasNext()) {
                        JCRNodeWrapper currentActivityNode = ((JCRNodeWrapper) myConnectionActivitiesIterator.nextNode());
                        JahiaActivityImpl activityImpl = new JahiaActivityImpl(currentActivityNode);
                        if (appId == null || (activityImpl.getAppId() == null)) {
                            result.add(new JahiaActivityImpl(currentActivityNode));
                        } else if (activityImpl.getAppId().equals(appId)) {
                            result.add(new JahiaActivityImpl(currentActivityNode));
                        }
                    }
                    return null;
                }
            });
            return ImmediateFuture.newInstance(new RestfulCollection<Activity>(result));
        } catch (RepositoryException re) {
            throw new ProtocolException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, re.getMessage(),
                    re);
        }
    }

    public Future<Activity> getActivity(UserId userId, GroupId groupId, final String appId, Set<String> fields, final String activityId, SecurityToken token) throws ProtocolException {
        try {
            Activity activity = jcrTemplate.doExecuteWithSystemSession(new JCRCallback<Activity>() {
                public Activity doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    // now let's retrieve the activities for the user.
                    JCRNodeWrapper activityNode = session.getNodeByUUID(activityId);

                    /* todo we might want to add validity checks for the users and groups here */

                    if (activityNode != null) {
                        return new JahiaActivityImpl(activityNode);
                    } else {
                        return null;
                    }
                }
            });
            return ImmediateFuture.newInstance(activity);
        } catch (RepositoryException re) {
            throw new ProtocolException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, re.getMessage(),
                    re);
        }
    }

    public Future<Void> deleteActivities(UserId userId, GroupId groupId, String appId, Set<String> activityIds, SecurityToken token) throws ProtocolException {
        return null;
    }

    public Future<Void> createActivity(UserId userId, GroupId groupId, String appId, Set<String> fields, Activity activity, SecurityToken token) throws ProtocolException {
        return null;
    }

    /* -- APPDATA SERVICE IMPLEMENTATION -- */

    public Future<DataCollection> getPersonData(Set<UserId> userIds, GroupId groupId, String appId, Set<String> fields, SecurityToken token) throws ProtocolException {
        return null;
    }

    public Future<Void> deletePersonData(UserId userId, GroupId groupId, String appId, Set<String> fields, SecurityToken token) throws ProtocolException {
        return null;
    }

    public Future<Void> updatePersonData(UserId userId, GroupId groupId, String appId, Set<String> fields, Map<String, String> values, SecurityToken token) throws ProtocolException {
        return null;
    }

    private String getUserNameFromKey(String userKey) {
        if (userKey.indexOf("}") >= 0) {
            return userKey.split("}")[1];
        } else {
            return userKey;
        }
    }

    private Node getUsersNode(Session session, String userKey) throws RepositoryException {
        String name = getUserNameFromKey(userKey);
        return session.getNode(jahiaUserManagerService.getUserSplittingRule().getPathForUsername(name));
    }

    private Map<String, Object> getPersonAppData(final String id, final Set<String> fields) throws RepositoryException {
        return jcrTemplate.doExecuteWithSystemSession(new JCRCallback<Map<String, Object>>() {
            public Map<String, Object> doInJCR(JCRSessionWrapper session) throws RepositoryException {
                Map<String, Object> appData = null;
                Node userNode = getUsersNode(session, id);
                Node appDataFolderNode;
                if (!userNode.hasNode("appdata")) {
                    appDataFolderNode = userNode.addNode("appdata", Constants.JAHIANT_CONTENTLIST);
                    session.save();
                } else {
                    appDataFolderNode = userNode.getNode("appdata");
                }
                if (appDataFolderNode != null) {
                    if (fields.contains(Person.Field.APP_DATA.toString())) {
                        appData = Maps.newHashMap();
                        @SuppressWarnings(
                                "unchecked") PropertyIterator dataPropertyIterator = appDataFolderNode.getProperties();
                        while (dataPropertyIterator.hasNext()) {
                            Property currentDataProperty = dataPropertyIterator.nextProperty();
                            appData.put(currentDataProperty.getName(), currentDataProperty.getValue());
                        }
                    } else {
                        String appDataPrefix = Person.Field.APP_DATA.toString() + ".";
                        for (String field : fields) {
                            if (field.startsWith(appDataPrefix)) {
                                if (appData == null) {
                                    appData = Maps.newHashMap();
                                }

                                String appDataField = field.substring(appDataPrefix.length());
                                if (appDataFolderNode.hasProperty(appDataField)) {
                                    appData.put(appDataField, appDataFolderNode.getProperty(appDataField).getValue());
                                }
                            }
                        }
                    }
                }
                return appData;
            }
        });
    }

    /**
     * This will load all the properties from the JCR into the Person object, using introspection to find property
     * and convert them appropriately.
     *
     * @param jahiaUser the JahiaUser to use to create the Shindig Person.
     */
    private JahiaPersonImpl loadPersonPropertiesFromJCR(final JahiaUser jahiaUser) throws RepositoryException {
        return jcrTemplate.doExecuteWithSystemSession(new JCRCallback<JahiaPersonImpl>() {
            public JahiaPersonImpl doInJCR(JCRSessionWrapper session) throws RepositoryException {
                JahiaPersonImpl jahiaPersonImpl = new JahiaPersonImpl(jahiaUser);
                String name = getUserNameFromKey(jahiaUser.getUserKey());
                Node usersFolderNode = session.getNode(jahiaUserManagerService.getUserSplittingRule().getPathForUsername(name));
                JCRUser jcrUser = null;
                if (!usersFolderNode.getProperty(JCRUser.J_EXTERNAL).getBoolean()) {
                    jcrUser = new JCRUser(usersFolderNode.getIdentifier());
                } else {
                    return null;
                }

                return jahiaPersonImpl;
            }
        });
    }

    /**
     * Get the set of user id's from a user and group
     */
    private Set<String> getIdSet(UserId user, GroupId group, SecurityToken token) throws RepositoryException {
        final String userId = user.getUserId(token);

        if (group == null) {
            return ImmutableSortedSet.of(userId);
        }

        final Set<String> returnVal = Sets.newLinkedHashSet();
        switch (group.getType()) {
            case all:
            case friends:
                jcrTemplate.doExecuteWithSystemSession(new JCRCallback<Object>() {
                    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        String userName = getUserNameFromKey(userId);

                        Node userNode = null;
                        try {
                            userNode = session.getNode(jahiaUserManagerService.getUserSplittingRule().getPathForUsername(userName));
                        } catch (PathNotFoundException pnfe) {
                            return returnVal;
                        }
                        Query myConnectionsQuery = session.getWorkspace().getQueryManager().createQuery("select * from [jnt:socialConnection] as uC where isdescendantnode(uC,['" + userNode.getPath() + "'])", Query.JCR_SQL2);
                        QueryResult myConnectionsResult = myConnectionsQuery.execute();

                        NodeIterator myConnectionsIterator = myConnectionsResult.getNodes();
                        while (myConnectionsIterator.hasNext()) {
                            JCRNodeWrapper myConnectionNode = (JCRNodeWrapper) myConnectionsIterator.nextNode();
                            JCRNodeWrapper connectedToNode = (JCRNodeWrapper) myConnectionNode.getProperty("j:connectedTo").getNode();
                            JahiaUser jahiaUser = jahiaUserManagerService.lookupUser(connectedToNode.getName());
                            if (jahiaUser != null) {
                                returnVal.add(jahiaUser.getUserKey());
                            }
                        }
                        return returnVal;
                    }
                });
                break;
            case groupId:
                break;
            case self:
                returnVal.add(userId);
                break;
        }
        return returnVal;
    }

    /**
     * Get the set of user id's for a set of users and a group
     */
    private Set<String> getIdSet(Set<UserId> users, GroupId group, SecurityToken token)
            throws RepositoryException {
        Set<String> ids = Sets.newLinkedHashSet();
        for (UserId user : users) {
            ids.addAll(getIdSet(user, group, token));
        }
        return ids;
    }

    /* -- APPDATA SERVICE IMPLEMENTATION -- */

    public Future<RestfulCollection<MessageCollection>> getMessageCollections(UserId userId, Set<String> fields, CollectionOptions options, SecurityToken token) throws ProtocolException {
        return null;
    }

    public Future<MessageCollection> createMessageCollection(UserId userId, MessageCollection msgCollection, SecurityToken token) throws ProtocolException {
        return null;
    }

    public Future<Void> modifyMessageCollection(UserId userId, MessageCollection msgCollection, SecurityToken token) throws ProtocolException {
        return null;
    }

    public Future<Void> deleteMessageCollection(UserId userId, String msgCollId, SecurityToken token) throws ProtocolException {
        return null;
    }

    public Future<RestfulCollection<Message>> getMessages(UserId userId, String msgCollId, Set<String> fields, List<String> msgIds, CollectionOptions options, SecurityToken token) throws ProtocolException {
        return null;
    }

    public Future<Void> createMessage(UserId userId, String appId, String msgCollId, Message message, SecurityToken token) throws ProtocolException {
        return null;
    }

    public Future<Void> deleteMessages(UserId userId, String msgCollId, List<String> ids, SecurityToken token) throws ProtocolException {
        return null;
    }

    public Future<Void> modifyMessage(UserId userId, String msgCollId, String messageId, Message message, SecurityToken token) throws ProtocolException {
        return null;
    }

    /*
    private <T> T filterFields(JSONObject object, Set<String> fields, Class<T> clz)
            throws JSONException {
        if (!fields.isEmpty()) {
            // Create a copy with just the specified fields
            object = new JSONObject(object, fields.toArray(new String[fields.size()]));
        }
        return converter.convertToObject(object.toString(), clz);
    }
    */

}
