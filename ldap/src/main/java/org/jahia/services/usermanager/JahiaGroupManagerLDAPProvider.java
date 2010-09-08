/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.services.usermanager;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.CannotProceedException;
import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.NoInitialContextException;
import javax.naming.PartialResultException;
import javax.naming.ServiceUnavailableException;
import javax.naming.SizeLimitExceededException;
import javax.naming.TimeLimitExceededException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.cache.Cache;
import org.jahia.services.cache.CacheService;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSiteTools;
import org.jahia.utils.JahiaTools;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 *
 * @author Viceic Predrag <Predrag.Viceic@ci.unil.ch>
 * @version 1.0
 */

public class JahiaGroupManagerLDAPProvider extends JahiaGroupManagerProvider {
// ------------------------------ FIELDS ------------------------------

    // the LDAP Group cache name.
    public static final String LDAP_GROUP_CACHE = "LDAPGroupsCache";

    /** the overall provider Group cache name. */
    public static final String PROVIDERS_GROUP_CACHE = "ProvidersGroupsCache";
    public static final String USERS_GROUPNAME = null;
    public static final String ADMINISTRATORS_GROUPNAME = null;
    public static final String GUEST_GROUPNAME = null;

    public static final String PROVIDER_NAME = "ldap";

    /** logging */
    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (JahiaGroupManagerLDAPProvider.class);

    private static String CONTEXT_FACTORY_PROP = "context.factory";
    private static String LDAP_URL_PROP = "url";
    private static String AUTHENTIFICATION_MODE_PROP = "authentification.mode";
    private static String PUBLIC_BIND_DN_PROP = "public.bind.dn";
    private static String PUBLIC_BIND_PASSWORD_PROP = "public.bind.password";

    private static String PRELOAD_GROUP_MEMBERS = "preload";

    private static String SEARCH_ATTRIBUTE_PROP = "search.attribute";
    private static String SEARCH_NAME_PROP = "search.name";
    private static String GROUP_OBJECTCLASS_ATTRIBUTE = "search.objectclass";
    private static String DYNGROUP_OBJECTCLASS_ATTRIBUTE = "dynamic.search.objectclass";

    private static String SEARCH_COUNT_LIMIT_PROP = "search.countlimit";
    private static String SEARCH_WILDCARD_ATTRIBUTE_LIST = "search.wildcards.attributes";

    private static String GROUP_MEMBERS_ATTRIBUTE = "members.attribute";
    private static String DYNGROUP_MEMBERS_ATTRIBUTE = "dynamic.members.attribute";

    private static String LDAP_REFFERAL_PROP = "refferal";
    private static String USE_CONNECTION_POOL = "groups.ldap.connect.pool";

    private static String AD_RANGE_STEP = "ad.range.step";    
    
    /**
     * Added to handle the fact that the attribute used to define group users is
     * not always a DN or named as a DN...
     */
    private static String SEARCH_USER_ATTRIBUTE_NAME = "members.user.attibute.map";

    private Cache<String, JahiaGroup> mProvidersGroupCache;

    private Properties ldapProperties = null;

    private List<String> searchWildCardAttributeList = null;

    //in order to avoid the continuous LDAP lookups due to lookupGroup("administrators:0,1,..")
    private List<String> nonExistentGroups = null;

    // Reference to the provider containing users of this group provider
    private JahiaUserManagerLDAPProvider userProvider;

    private CacheService cacheService;

    private JahiaUserManagerService jahiaUserManagerService;

// -------------------------- STATIC METHODS --------------------------

    private static boolean containsMembersRange(Attributes attrs,
            String membersAttribute) throws NamingException {
        
        boolean rangePresent = false;
        String rangeAttribute = membersAttribute + ";range=";
        NamingEnumeration<String> ids = attrs.getIDs();
        while (ids.hasMore() && !rangePresent) {
            String attrId = ids.next();
            rangePresent = attrId.toLowerCase().startsWith(rangeAttribute);
        }

        return rangePresent;
    }

    private static String escapeFilterValue(String value) {
        String filterValue = JahiaTools.replacePattern(value, "\\", "\\5c");
        filterValue = JahiaTools.replacePattern(filterValue, "(", "\\28");
        filterValue = JahiaTools.replacePattern(filterValue, ")", "\\29");

        return filterValue;
    }
    
    private static void loadMembersUsingRange(SearchResult sr, DirContext ctx, SearchControls searchCtl,
            String filterString, String searchNameProp, String groupNameAttribute, String membersAttributeName,
            int rangeStep) throws NamingException {
        
        if (logger.isDebugEnabled()) {
            logger.debug("Loading mambers for group entry '" + sr.getName() + "'");
        }
        
        // backup originally requested attributes
        String[] originalReturningAttributes = searchCtl.getReturningAttributes();

        int lowRange = 0;
        int highRange = lowRange + rangeStep;
        boolean finished = false;
        Attribute membersAttribute = new BasicAttribute(membersAttributeName);

        while (!finished) {
            finished = false;
            // Specify the attributes to return
            String rangeAttributeName = membersAttributeName + ";range=" + lowRange + "-" + highRange;
            searchCtl.setReturningAttributes(new String[] { rangeAttributeName });
            StringBuilder filter = new StringBuilder(filterString);
            filter.insert(0, "(&").append("(").append(groupNameAttribute).append("=").append(
                    escapeFilterValue(sr.getAttributes().get(groupNameAttribute).get().toString())).append("))");

            if (logger.isDebugEnabled()) {
                logger.debug("Retrieving attribute values range for attribute '" + rangeAttributeName + "'");
            }

            // Search for objects using the filter
            NamingEnumeration<SearchResult> searchResults = ctx.search(searchNameProp, filter.toString(), searchCtl);
            while (searchResults.hasMore()) {
                SearchResult groupResult = (SearchResult) searchResults.next();
                if (logger.isDebugEnabled()) {
                    logger.debug("Got result '" + groupResult.getName() + "' with attributes: " + groupResult.getAttributes());
                }
                
                if (groupResult.getName().equals(sr.getName())) {
                    NamingEnumeration<String> attributes = groupResult.getAttributes().getIDs();
                    boolean memberAttributePresent = false;
                    while (attributes.hasMore()) {
                        String attrId = attributes.next();
                        if (attrId.startsWith(membersAttributeName)) {
                            memberAttributePresent = true;
                            Attribute members = groupResult.getAttributes().get(attrId);
                            if (logger.isDebugEnabled()) {
                                logger.debug("Found attribute '" + attrId + "' with members: " + members.get());
                            }
                            for (NamingEnumeration<?> ae = members.getAll(); ae.hasMore();) {
                                membersAttribute.add(ae.next());
                            }
                            if (attrId.endsWith("*")) {
                                // finish if we got all members (* indicate last
                                // range of values)
                                finished = true;
                                if (logger.isDebugEnabled()) {
                                    logger.debug("We got last value chunk, so we are done");
                                }
                            }
                        }
                    }
                    if (!memberAttributePresent) {
                        // also finish if there are no members at all
                        finished = true;
                        if (logger.isDebugEnabled()) {
                            logger.debug("No members attribute found, so we are done");
                        }
                    }
                } else {
                    logger.warn("Search for a group '" + sr.getName() + "' (" + sr.getNameInNamespace() + " ::: "
                            + groupNameAttribute + "=" + sr.getAttributes().get(groupNameAttribute).get().toString()
                            + ") returned another entry: " + groupResult.getName());
                }

            }
            // Changing the range
            lowRange = highRange + 1;
            highRange = lowRange + rangeStep;
        }

        // set found members
        sr.getAttributes().put(membersAttribute);

        // restore search attributes
        searchCtl.setReturningAttributes(originalReturningAttributes);
    }

// --------------------------- CONSTRUCTORS ---------------------------

    /**
     * Default constructor.
     *
     * @throws JahiaException Raise a JahiaException when during initialization
     *                        one of the needed services could not be instanciated.
     */
    protected JahiaGroupManagerLDAPProvider ()
            throws JahiaException {
        nonExistentGroups = new ArrayList<String>();
        nonExistentGroups.add ("administrators");
        nonExistentGroups.add ("guest");
        nonExistentGroups.add ("users");
    }

// --------------------- GETTER / SETTER METHODS ---------------------

    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    public void setJahiaUserManagerService(JahiaUserManagerService jahiaUserManagerService) {
        this.jahiaUserManagerService = jahiaUserManagerService;
    }

    public void setLdapProperties(Map<Object, Object> ldapProperties) {
        this.ldapProperties = new Properties();
        this.ldapProperties.putAll(ldapProperties);
    }

    public void start()
            throws JahiaInitializationException {
        // instanciates the cache
        mProvidersGroupCache = cacheService.createCacheInstance(PROVIDERS_GROUP_CACHE);

        String wildCardAttributeStr = ldapProperties.getProperty (JahiaGroupManagerLDAPProvider.
                SEARCH_WILDCARD_ATTRIBUTE_LIST);
        if (wildCardAttributeStr != null) {
            this.searchWildCardAttributeList = new ArrayList<String>();
            StringTokenizer wildCardTokens = new StringTokenizer (
                    wildCardAttributeStr, ", ");
            while (wildCardTokens.hasMoreTokens ()) {
                String curAttrName = wildCardTokens.nextToken ().trim ();
                this.searchWildCardAttributeList.add (curAttrName);
            }
        }

        logger.debug ("Initialized and connected to public repository");
    }

    public void stop() {}

    /**
     * Create a new group in the system.
     *
     * @param siteID    the site owner of this user
     * @param groupname Group's unique identification name
     *
     * @param hidden
     * @return Retrun a reference on a group object on success, or if the groupname
     *         already exists or another error occured, null is returned.
     */

    public JahiaGroup createGroup(int siteID, String groupname,
                                  Properties parm3, boolean hidden) {
        /**@todo Implement this org.jahia.services.usermanager.JahiaGroupManagerProvider abstract method*/
        throw new java.lang.UnsupportedOperationException (
                "Method createGroup() not yet implemented.");
    }

    public boolean deleteGroup (JahiaGroup group) {
        return false;
    }

    // @author  AK
    /**
     * Get all JahiaSite objects where the user has an access.
     *
     * @param user the user you want to get his access grantes sites list.
     *
     * @return Return a List containing all JahiaSite objects where the user has an access.
     */

    public List<JahiaSite> getAdminGrantedSites (JahiaUser user)  {
        List<JahiaSite> grantedSites = new ArrayList<JahiaSite>();
        try {
            Iterator<JahiaSite> sitesList = ServicesRegistry.getInstance ().
                    getJahiaSitesService ().getSites ();

            while (sitesList.hasNext ()) {
                JahiaSite jahiaSite = sitesList.next ();
                logger.debug ("check granted site " + jahiaSite.getSiteKey());

                if (JahiaSiteTools.getAdminGroup (jahiaSite).isMember (user)) {
                    logger.debug ("granted site for " + jahiaSite.getSiteKey());
                    grantedSites.add (jahiaSite);
                }
            }
        } catch (JahiaException e) {
            logger.error("getAdminGrantedSites",e);
        }

        return grantedSites;
    }

    public JahiaGroup getAdministratorGroup (int siteID) {
        if (ADMINISTRATORS_GROUPNAME != null)
            return lookupGroup (siteID, ADMINISTRATORS_GROUPNAME);
        else
            return null;
    }

    // @author  NK
    /**
     * Return a <code>List</code) of <code>String</code> representing all the
     * group keys of a site.
     *
     * @param siteID the site id
     *
     * @return Return a List of identifier of all groups of this site.
     */

    public List<String> getGroupList (int siteID) {
        return getGroupList ();
    }

    /**
     * Modified by EP : 2004-18-06
     * Changes the way to retrieve groups members.
     * The properties file indicates a value for the attribute name that is stored
     * into the GROUP_MEMBERS_ATTRIBUTE, the use it instead of parsing the DN
     */
    public Map<String, Principal> getGroupMembers (String groupKey, boolean dynamic) {
        Map<String, Principal> members = null;
        DirContext ctx = null;
        try {
            ctx = getPublicContext ();
            SearchResult sr = getPublicGroup (ctx, groupKey);
            members = getGroupMembers(sr, dynamic);
        } catch (NamingException ne) {
            logger.warn ("JNDI warning",ne);
        } finally {
            invalidateCtx(ctx);
        }
        return members;
    }

    /**
     * Return a <code>List</code) of <code>String</code> representing all the
     * group names.
     *
     * @return Return a List of strings containing all the group names.
     */

    public List<String> getGroupnameList () {
        return getGroupList ();
    }

    // @author  NK
    /**
     * Return a <code>List</code) of <code>String</code> representing all the
     * group keys of a site.
     *
     * @return Return a List of identifier of all groups of this site.
     */
    public List<String> getGroupList () {
        List<String> result = new ArrayList<String>();

        DirContext ctx = null;
        try {
            ctx = getPublicContext ();
            for (SearchResult sr : getGroups (ctx, null)) {
                JahiaGroup curGroup = ldapToJahiaGroup (sr);
                if (curGroup != null) {
                    result.add (curGroup.getGroupKey ());
                }
            }
        } catch (SizeLimitExceededException slee) {
            // we just return the list as it is
            logger.debug ("Search generated more than configured maximum search limit, limiting to " +
                    this.ldapProperties.getProperty (SEARCH_COUNT_LIMIT_PROP) +
                    " first results...");
        } catch (NamingException ne) {
            logger.warn ("JNDI warning",ne);
            result = new ArrayList<String>();
        } finally {
            invalidateCtx(ctx);
        }

        return result;
    }

    /**
     * Return a <code>List</code) of <code>String</code> representing all the
     * group names of a site.
     *
     * @param siteID the site id
     *
     * @return Return a List of strings containing all the group names.
     */

    public List<String> getGroupnameList (int siteID) {
        List<String> result = new ArrayList<String>();

        DirContext ctx = null;
        try {
            ctx = getPublicContext ();
            for (SearchResult sr : getGroups (ctx, null)) {
                JahiaGroup curGroup = ldapToJahiaGroup (sr);
                if (curGroup != null) {
                    result.add (curGroup.getGroupname ());
                }
            }
        } catch (SizeLimitExceededException slee) {
            // we just return the list as it is
            logger.debug ("Search generated more than configured maximum search limit in, limiting to " +
                    this.ldapProperties.getProperty (SEARCH_COUNT_LIMIT_PROP) +
                    " first results...");
        } catch (NamingException ne) {
            logger.warn ("JNDI warning",ne);
            result = new ArrayList<String>();
        } finally {
            invalidateCtx(ctx);
        }

        return result;
    }

    /**
     * Retrieves groups from the LDAP public repository.
     *
     * @param ctx     the current context in which to search for the group
     * @param filters a set of name=value string that contain RFC 2254 format
     *                filters in the value, or null if we want to look in the full repository
     *
     * @return a list of SearchResult objects
     *         that contains the LDAP group entries that correspond to the filter
     *
     * @throws NamingException
     */
    private List<SearchResult> getGroups (DirContext ctx, Properties filters)
            throws NamingException {
        if (ctx == null) {
            throw new NamingException ("Context is null !");
        }

        StringBuffer filterString = new StringBuffer();
        filterString.append("(|(objectClass=").append(
                ldapProperties.getProperty (JahiaGroupManagerLDAPProvider.GROUP_OBJECTCLASS_ATTRIBUTE, "groupOfNames")).append(
                ")(objectClass=").append(
                ldapProperties.getProperty (JahiaGroupManagerLDAPProvider.DYNGROUP_OBJECTCLASS_ATTRIBUTE, "groupOfURLs")).append("))");

        if (filters == null) {
            filters = new Properties ();
        }

        // let's translate Jahia properties to LDAP properties
        mapJahiaPropertiesToLDAP (filters);

        if (filters.size () > 0) {
            filterString.insert(0,"(&");

            Iterator<?> filterKeys = filters.keySet().iterator();
            while (filterKeys.hasNext ()) {
                String filterName = (String) filterKeys.next ();
                String filterValue = filters.getProperty (filterName);
                // we do all the RFC 2254 replacement *except* the "*" character
                // since this is actually something we want to use.
                filterValue = escapeFilterValue(filterValue);

                if ("*".equals (filterName)) {
                    // we must match the value for all the attributes
                    // declared in the property file.
                    if (this.searchWildCardAttributeList != null) {
                        if (this.searchWildCardAttributeList.size () > 1) {
                            filterString.append ("(|");
                        }
                        Iterator<String> attributeEnum = this.
                                searchWildCardAttributeList.iterator();
                        while (attributeEnum.hasNext ()) {
                            String curAttributeName = attributeEnum.next ();
                            filterString.append ("(");
                            filterString.append (curAttributeName);
                            filterString.append ("=");
                            filterString.append (filterValue);
                            filterString.append (")");
                        }
                        if (this.searchWildCardAttributeList.size () > 1) {
                            filterString.append (")");
                        }
                    }
                } else {
                    filterString.append ("(");
                    filterString.append (filterName);
                    filterString.append ("=");
                    filterString.append (filterValue);
                    filterString.append (")");
                }
            }

            filterString.append (")");
        }

        // Search for objects that have those matching attributes
        SearchControls searchCtl = new SearchControls ();
        searchCtl.setSearchScope (SearchControls.SUBTREE_SCOPE);
        int countLimit = Integer.parseInt (ldapProperties.getProperty (SEARCH_COUNT_LIMIT_PROP));
        searchCtl.setCountLimit (countLimit);
        return getGroups(ctx, searchCtl, filterString);
    }

    /**
     * Maps Jahia group to LDAP properties using the definition
     * mapping in the group LDAP configuration properties file. This modifies
     * the groupProps
     *
     * @param groupProps
     */
    private void mapJahiaPropertiesToLDAP (Properties groupProps) {
        for (Iterator<?> iterator = ldapProperties.keySet().iterator(); iterator.hasNext();) {
            String key = (String) iterator.next();
            if (key.endsWith(".attribute.map")) {
                String jahiaProperty = key.substring(0,key.length()-14);
                String curProperty = ldapProperties.getProperty(key);
                if (groupProps.getProperty (jahiaProperty) != null) {
                    groupProps.put(curProperty, groupProps.remove(jahiaProperty));
                }
            }
        }
        if (groupProps.containsKey("members")) {
            groupProps.put(ldapProperties.get(GROUP_MEMBERS_ATTRIBUTE), groupProps.remove("members"));
        }
    }

    /**
     * Returns the internal public context variable. The point of this is to
     * keep this connection open as long as possible, in order to reuser the
     * connection.
     *
     *
     * @return DirContext the current public context.
     */
    public DirContext getPublicContext() throws NamingException {
        DirContext publicCtx = null;
        try {
            publicCtx = connectToPublicDir();
        } catch (NamingException ne) {
            logger.warn("JNDI warning", ne);
        }
        return publicCtx;
    }

    private DirContext connectToPublicDir ()
            throws NamingException {
        /*// EP : 2004/29/06 : implement reconnection mechanism on ldap...
        if (((JahiaGroupManagerRoutingService)ServicesRegistry
        					.getInstance()
        					.getJahiaGroupManagerService())
        					.getServerList(PROVIDER_NAME) != null) {
        	logger.debug("connecting to all public dir for groups");
        	return connectToAllPublicDir();
        }*/

        // Identify service provider to use
        logger.debug ("Attempting connection to LDAP repository on " +
                ldapProperties.getProperty (LDAP_URL_PROP) + "...");
        Hashtable<String, String> publicEnv = new Hashtable<String, String>(11);
        publicEnv.put (Context.INITIAL_CONTEXT_FACTORY,
                ldapProperties.getProperty (CONTEXT_FACTORY_PROP));
        publicEnv.put (Context.PROVIDER_URL,
                ldapProperties.getProperty (LDAP_URL_PROP));
        publicEnv.put (Context.SECURITY_AUTHENTICATION,
                ldapProperties.getProperty (AUTHENTIFICATION_MODE_PROP));
        publicEnv.put (Context.SECURITY_PRINCIPAL,
                ldapProperties.getProperty (PUBLIC_BIND_DN_PROP));
        publicEnv.put (Context.REFERRAL,
                ldapProperties.getProperty (LDAP_REFFERAL_PROP, "ignore"));
        // Enable connection pooling
        publicEnv.put("com.sun.jndi.ldap.connect.pool", ldapProperties
                .getProperty(USE_CONNECTION_POOL, "true"));                
        if (ldapProperties.getProperty (PUBLIC_BIND_PASSWORD_PROP) != null) {
            logger.debug ("Using authentification mode to connect to public dir...");
            publicEnv.put (Context.SECURITY_CREDENTIALS,
                    ldapProperties.getProperty (PUBLIC_BIND_PASSWORD_PROP));
        }

        // Create the initial directory context
        return new InitialDirContext (publicEnv);
    }

    /**
     * Translates LDAP attributes to a JahiaGroup properties set. Multi-valued
     * attribute values are converted to Strings containing LINEFEED (\n)
     * characters. This way it is quite simple to use String Tokenizers to
     * extract multiple values. Note that if a value ALREADY contains a line
     * feed characters this will cause unexpected behavior.
     *
     * @param sr       result of a search on a LDAP directory context
     * @return JahiaLDAPGroup a group initialized with the properties loaded
     *         from the LDAP database, or null if no groupKey could be determined for
     *         the group.
     */
    private JahiaLDAPGroup ldapToJahiaGroup (SearchResult sr) {
        JahiaLDAPGroup group;
        Properties groupProps = new Properties ();
        String usingGroupKey = null;
        Attributes attrs = sr.getAttributes ();

        Enumeration<?> attrsEnum = attrs.getAll ();
        while (attrsEnum.hasMoreElements()) {
            Attribute curAttr = (Attribute) attrsEnum.nextElement();
            String attrName = curAttr.getID ();
            StringBuffer attrValueBuf = new StringBuffer ();
            try {
                Enumeration<?>curAttrValueEnum = curAttr.getAll ();
                while (curAttrValueEnum.hasMoreElements()) {
                    Object curAttrValueObj = curAttrValueEnum.nextElement();
                    if ((curAttrValueObj instanceof String)) {
                        attrValueBuf.append ((String) curAttrValueObj);
                    } else {
                        logger.debug ("Converting attribute <" + attrName + "> from class " +
                                curAttrValueObj.getClass ().toString () + " to String...");
                        /** @todo FIXME : for the moment we convert everything to String */
                        attrValueBuf.append (curAttrValueObj);
                    }
                    attrValueBuf.append ('\n');
                }
            } catch (NamingException ne) {
                logger.warn ("JNDI warning",ne);
                attrValueBuf = new StringBuffer ();
            }
            String attrValue = attrValueBuf.toString ();
            if (attrValue.endsWith ("\n")) {
                attrValue = attrValue.substring (0, attrValue.length () - 1);
            }
            if ((attrName != null) && (attrValue != null)) {
                if (usingGroupKey == null) {
                    if (attrName.equals (ldapProperties.getProperty (
                            SEARCH_ATTRIBUTE_PROP))) {
                        usingGroupKey = attrValue;
                    }
                }
                groupProps.setProperty (attrName, attrValue);
                // hack for strange-case server
                if (attrName.equalsIgnoreCase("objectClass")) {
                    groupProps.setProperty ("objectClass", attrValue);
                }
            }
        }

        if (usingGroupKey != null) {
            mapLDAPToJahiaProperties (groupProps);
            // FIXME : Quick hack for merging Jahia DB group properties with LDAP group
            mapDBToJahiaProperties (groupProps, usingGroupKey);
            boolean dynamic = groupProps.getProperty("objectClass").indexOf(ldapProperties.getProperty (JahiaGroupManagerLDAPProvider.DYNGROUP_OBJECTCLASS_ATTRIBUTE, "groupOfURLs")) != -1;
            try {
                if (ldapProperties.getProperty(PRELOAD_GROUP_MEMBERS, "true").equalsIgnoreCase("true")) {
                    Map<String, Principal> members = null;
                    try {
                        members = getGroupMembers (sr, dynamic);
                    } catch (NamingException ne) {
                        logger.warn ("JNDI warning",ne);
                    }

                    group = new JahiaLDAPGroup (0, usingGroupKey, usingGroupKey, 0,
                            members,
                            groupProps, dynamic, true, this);
                } else {
                    group = new JahiaLDAPGroup (0, usingGroupKey, usingGroupKey, 0,
                            null,
                            groupProps, dynamic, false, this);
                }
                return group;
            } catch (JahiaException e) {
                logger.warn (e);
                return null;
            }
        } else {
            logger.debug ("Ignoring entry " + sr.getName () +
                    " because it has no valid " +
                    ldapProperties.getProperty (SEARCH_ATTRIBUTE_PROP) +
                    " attribute to be mapped onto user key...");
            return null;
        }
    }

    /**
     * Map LDAP properties to Jahia group properties.
     * This method modifies the groupProps object passed on parameters to add
     * the new properties.
     *
     * @param groupProps Group properties to check for mappings. Basically what
     *                   we do is copy LDAP properties to standard Jahia properties. This is
     *                   defined in the group ldap properties file. Warning this object is modified
     *                   by this method !
     *
     * @todo FIXME : if properties exist in LDAP that have the same name as
     * Jahia properties these will be erased. We should probably look into
     * making the properties names more unique such as org.jahia.propertyname
     */
    private void mapLDAPToJahiaProperties (Properties groupProps) {
        // copy attribute to standard Jahia properties if they exist both in
        // the mapping and in the repository
        for (Iterator<?> iterator = ldapProperties.keySet().iterator(); iterator.hasNext();) {
            String key = (String) iterator.next();
            if (key.endsWith(".attribute.map")) {
                String jahiaProperty = key.substring(0,key.length()-14);
                String curProperty = ldapProperties.getProperty(key);
                if (groupProps.getProperty (curProperty) != null) {
                    groupProps.setProperty (jahiaProperty, groupProps.getProperty (curProperty));
                }
            }
        }
    }

    /**
     * Retrieves properties from internal jahia DB
     *
     * @param groupProps    the group properties to set
     * @param usingGroupKey the group whose the properties has to be extracted.
     */
    private void mapDBToJahiaProperties (Properties groupProps,
                                         String usingGroupKey) {
            // Get all the user attributes
//        Properties dbProperties = groupManager.getGroupProperties (-1, PROVIDER_NAME, usingGroupKey);
//            groupProps.putAll (dbProperties);
    }

    private Map<String, Principal> getGroupMembers (SearchResult sr, boolean dynamic)
            throws NamingException {
        Map<String, Principal> members = new ConcurrentHashMap<String, Principal> ();
        Attributes attr = sr.getAttributes ();
        NamingEnumeration<?> answer = null;
        try {
            if (dynamic) {
                answer = attr.get (ldapProperties.getProperty (
                        DYNGROUP_MEMBERS_ATTRIBUTE, "memberurl")).
                        getAll ();
            } else {
                answer = attr.get (ldapProperties.getProperty (
                        GROUP_MEMBERS_ATTRIBUTE)).
                        getAll ();
            }
        } catch (NullPointerException ne) {
            logger.debug ("No members");
        }

//        // test if the properties file contains the SEARCH_USER_ATTRIBUTE_NAME value
//        if (ldapProperties.getProperty(SEARCH_USER_ATTRIBUTE_NAME) == null) {
//        	logger.error("Missing the attribute type stored in the " + GROUP_MEMBERS_ATTRIBUTE + " group attribute.");
//        	return members;
//        }

        // EP : 2004-21-07 : refactoring : uses old way if SEARCH_USER_ATTRIBUTE_NAME not defined
        String searchProperties = ldapProperties.getProperty (SEARCH_USER_ATTRIBUTE_NAME);
        boolean searchUserDefined = (!(searchProperties == null) && (searchProperties.length() > 0));

        logger.debug ("Getting members for group, dynamic="+dynamic+", searchUserDefined="+searchUserDefined);

        if (answer != null) {
            while (answer.hasMore ()) {
                String userKey = (String)answer.next ();
                if (dynamic) {
                    Properties p = new Properties();
                    p.put("ldap.url", userKey);
                    Set<JahiaUser> t = getUserManagerProvider().searchUsers(p);
                    for (Iterator<JahiaUser> iterator = t.iterator(); iterator.hasNext();) {
                        JahiaUser jahiaUser = iterator.next();
                        members.put(jahiaUser.getUserKey(), jahiaUser);
                    }
                } else {
                    JahiaUser user = null;
                    if (searchUserDefined) { // use attribute definition (?)
                        user = getUserManagerProvider().
                                lookupUserByKey(userKey,
                                        ldapProperties
                                        .getProperty (
                                        SEARCH_USER_ATTRIBUTE_NAME));
                    } else { // use DN
                        user = getUserManagerProvider()
                                        .lookupUserFromDN(
                                                userKey);
                    }
                    if (user != null)
                        members.put (user.getUserKey(), user);
                }
            }
        }
        return members;
    }

    private void invalidateCtx (DirContext ctx) {
        if (ctx == null) {
            logger.debug ("Context passed is null, ignoring it...");
            return;
        }
        try {
            ctx.close ();
        } catch (Exception e) {
            logger.warn (e);
        } finally {
        	ctx = null;
        }
    }

    /**
     * Return an instance of the guest group
     *
     * @return Return the instance of the guest group. Return null on any failure.
     */

    public JahiaGroup getGuestGroup (int siteID) {
        if (GUEST_GROUPNAME != null)
            return lookupGroup (siteID, GUEST_GROUPNAME);
        else
            return null;
    }

    public synchronized JahiaUserManagerLDAPProvider getUserManagerProvider() {
        if (userProvider == null) {
            List<? extends JahiaUserManagerProvider> v = jahiaUserManagerService.getProviderList();
            for (Iterator<? extends JahiaUserManagerProvider> iterator = v.iterator(); iterator.hasNext();) {
                JahiaUserManagerProvider userManagerProviderBean = (JahiaUserManagerProvider) iterator.next();
                if (userManagerProviderBean.getClass().getName().equals(JahiaUserManagerLDAPProvider.class.getName())) {
                    JahiaUserManagerLDAPProvider jahiaUserManagerLDAPProvider = (JahiaUserManagerLDAPProvider)userManagerProviderBean;
                    if (jahiaUserManagerLDAPProvider.getUrl().equals(ldapProperties.get(LDAP_URL_PROP))) {
                        userProvider = jahiaUserManagerLDAPProvider;
                    }
                }
            }
        }
        return userProvider;
    }

    /**
     * Return the list of groups to which the specified user has access.
     *
     * @param user Valid reference on an existing group.
     *
     * @return Return a List of strings holding all the group names to
     *         which the user as access. On any error, the returned List
     *         might be empty.
     */

    public List<String> getUserMembership (JahiaUser user) {
        if (! (user instanceof JahiaLDAPUser)) {
            return new ArrayList<String>();
        }

        List<String> result = ((JahiaLDAPUser)user).getGroups();
        if (result != null) {
            return result;
        }

        result = new ArrayList<String>();

        StringBuffer filterBuffer = new StringBuffer ();
        filterBuffer.append ("(&(objectclass=");
        filterBuffer.append (ldapProperties.getProperty (JahiaGroupManagerLDAPProvider.
                GROUP_OBJECTCLASS_ATTRIBUTE, "groupOfNames"));
        filterBuffer.append (")(");
        filterBuffer.append (ldapProperties.getProperty (GROUP_MEMBERS_ATTRIBUTE)); //memberUid
        filterBuffer.append ("="); //=
        filterBuffer.append ( ((JahiaLDAPUser) user).getDN() ); //user
        filterBuffer.append ("))");

        SearchControls searchCtl = new SearchControls ();
        searchCtl.setSearchScope (SearchControls.SUBTREE_SCOPE);
        String[] retattrs = new String[1];
        retattrs[0] = ldapProperties.getProperty (SEARCH_ATTRIBUTE_PROP);
        searchCtl.setReturningAttributes (retattrs);

        DirContext ctx = null;
        try {
            ctx = getPublicContext();
            for (SearchResult searchResult : getGroups(ctx, searchCtl, filterBuffer)) {
                String groupKey = searchResult.
                        getAttributes ().
                        get (ldapProperties.getProperty (
                                SEARCH_ATTRIBUTE_PROP)).get ().
                        toString ();
                result.add ("{ldap}"+groupKey);
                logger.debug("groupKey=" + groupKey);
            }

            // Now look for dynamic groups
            filterBuffer = new StringBuffer();
            filterBuffer.append("(objectclass=");
            filterBuffer
                    .append(ldapProperties
                            .getProperty(
                                    JahiaGroupManagerLDAPProvider.DYNGROUP_OBJECTCLASS_ATTRIBUTE,
                                    "groupOfURLs"));
            filterBuffer.append(")");

            searchCtl = new SearchControls();
            searchCtl.setSearchScope(SearchControls.SUBTREE_SCOPE);
            searchCtl.setReturningAttributes(new String[] {
                    ldapProperties.getProperty(SEARCH_ATTRIBUTE_PROP),
                    ldapProperties.getProperty(DYNGROUP_MEMBERS_ATTRIBUTE,
                            "memberurl") });

            for (SearchResult sr : getGroups(ctx, searchCtl, filterBuffer)) {
                Attributes attr = sr.getAttributes();
                String groupKey = attr.get(
                        ldapProperties.getProperty(SEARCH_ATTRIBUTE_PROP))
                        .get().toString();

                logger.debug ("groupKey=" + groupKey);
                NamingEnumeration<?> answer2 = null;
                Attribute attribute = attr.get (ldapProperties.getProperty (DYNGROUP_MEMBERS_ATTRIBUTE, "memberurl"));
                if (attribute != null) {
                    answer2 = attribute.getAll ();
                    while (answer2.hasMore ()) {
                        String url = (String)answer2.next ();
                        Properties p = new Properties();
                        p.put("ldap.url", url);
                        p.put("user.key", removeKeyPrefix(user.getUserKey()));
                        Set<JahiaUser> t = getUserManagerProvider().searchUsers(p);
                        if (!t.isEmpty()) {
                            result.add("{ldap}"+groupKey);
                            if (answer2.hasMore()) {
                                answer2.close();
                            }
                            break;
                        }
                    }
                }
            }
        } catch (NamingException e) {
            logger.warn (e);
            return new ArrayList<String>();
        } finally {
            invalidateCtx(ctx);
        }
        ((JahiaLDAPUser)user).setGroups(result);
        return result;
    }

    private List<SearchResult> getGroups(DirContext ctx, SearchControls searchCtl, StringBuffer filter) throws NamingException {
        List<SearchResult> answerList = new ArrayList<SearchResult>();
        try {
            answerList = doGroupSearch(ctx, searchCtl, filter);
        } catch (NoInitialContextException nice) {
            logger.debug("Reconnection required", nice);
        } catch (CannotProceedException cpe) {
            logger.debug("Reconnection required", cpe);
        } catch (ServiceUnavailableException sue) {
            logger.debug("Reconnection required", sue);
        } catch (TimeLimitExceededException tlee) {
            logger.debug("Reconnection required", tlee);
        } catch (CommunicationException ce) {
            logger.debug("Reconnection required", ce);
        } catch (javax.naming.SizeLimitExceededException limit) {
            logger.warn("Search count limit reached", limit);
        } catch (javax.naming.NamingException e) {
            logger.warn("Unable to retrieve all LDAP groups. Cause: " + e.getMessage(), e);
        }

        return answerList;
    }
    
    private List<SearchResult> doGroupSearch(DirContext ctx, SearchControls searchCtl, StringBuffer filter)
            throws NamingException {
        
        String filterString = filter.toString();
        // Search for objects that have those matching attributes
        if (logger.isDebugEnabled()) {
            logger.debug("Using filter string [" + filterString + "]...");
        }
        List<SearchResult> answerList = new ArrayList<SearchResult>();
        String searchNameProp = ldapProperties.getProperty(SEARCH_NAME_PROP);
        String groupSearchAttributeName = ldapProperties.getProperty(SEARCH_ATTRIBUTE_PROP);

        int rangeStep = Integer.parseInt(ldapProperties.getProperty(AD_RANGE_STEP, "0"));
        if (rangeStep == 0 || searchCtl.getReturningAttributes() != null) {
            NamingEnumeration<SearchResult> enumeration = ctx.search(searchNameProp, filterString, searchCtl);
            try {
	            while (enumeration.hasMore()) {
	                answerList.add(enumeration.next());
	            }
            } catch (SizeLimitExceededException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Search generated more than configured maximum search limit," + " limiting to "
                            + this.ldapProperties.getProperty(SEARCH_COUNT_LIMIT_PROP) + " first results...", e);
                } else {
                    logger.warn("Search generated more than configured maximum search limit," + " limiting to "
                            + this.ldapProperties.getProperty(SEARCH_COUNT_LIMIT_PROP) + " first results...");
                }
            }
        } else {
            String membersAttribute = ldapProperties.getProperty(GROUP_MEMBERS_ATTRIBUTE);

            try {
                NamingEnumeration<SearchResult> srcResults = ctx.search(searchNameProp, filterString, searchCtl);
                while (srcResults.hasMore()) {
                    SearchResult sr = (SearchResult) srcResults.next();
                    Attributes attrs = sr.getAttributes();
                    if (containsMembersRange(attrs, membersAttribute)) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Got range of members in group '" + sr.getName() + "'");
                        }
                        loadMembersUsingRange(sr, ctx, searchCtl, filterString, searchNameProp, groupSearchAttributeName,
                                membersAttribute, rangeStep);
                    }
                    answerList.add(sr);
                }
            } catch (SizeLimitExceededException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Search generated more than configured maximum search limit," + " limiting to "
                            + this.ldapProperties.getProperty(SEARCH_COUNT_LIMIT_PROP) + " first results...", e);
                } else {
                    logger.warn("Search generated more than configured maximum search limit," + " limiting to "
                            + this.ldapProperties.getProperty(SEARCH_COUNT_LIMIT_PROP) + " first results...");
                }
            }
        }

        return answerList;
    }

    private String removeKeyPrefix (String groupKey) {
        if (groupKey.startsWith ("{ldap}")) {
            return groupKey.substring (6);
        } else {
            return groupKey;
        }
    }

    /**
     * Return an instance of the users group.
     *
     * @return Return the instance of the users group. Return null on any failure
     */

    public JahiaGroup getUsersGroup (int siteID) {
        if (USERS_GROUPNAME != null)
            return lookupGroup (siteID, USERS_GROUPNAME);
        else
            return null;
    }

    /**
     * This function checks on a gived site if the groupname has already been
     * assigned to another group.
     *
     * @param siteID the site id
     * @param name   String representing the unique group name.
     *
     * @return Return true if the specified username has not been assigned yet,
     *         return false on any failure.
     */

    public boolean groupExists (int siteID, String name) {
        return (lookupGroup (siteID, name) != null);
    }

    /**
     * Lookup the group information from the underlaying system (DB, LDAP, ... )
     * Try to lookup the group into the cache, if it's not in the cache, then
     * load it into the cahce from the database.
     *
     * EP : 2004/23/07 : big refactoring
     *
     * @param siteID the site id
     * @param name   Group's unique identification name.
     *
     * @return Return a reference on a the specified group name. Return null
     *         if the group doesn't exist or when any error occured.
     */
    public JahiaGroup lookupGroup(int siteID, String name) {
        // try to avoid a NullPointerException
        if (name == null) {
            return null;
        }

        // String tmpGroupName = removeKeyPrefix (name);

        /*
         * 2004-16-06 : update by EP new cache to browse : cross providers ...
         */
        JahiaGroup group = mProvidersGroupCache.get("n" + siteID + "_" + name);
        if (group == null) {
            group = lookupGroupInLDAP(siteID, name);
            if (group != null) {
                /*
                 * 2004-16-06 : update by EP new cache to populate : cross providers ...
                 */
                mProvidersGroupCache.put("k" + group.getGroupKey(), group);
                // with name for speed
                mProvidersGroupCache.put("n" + group.getSiteID() + "_"
                        + group.getGroupname(), group);
            }
        }

        return group;
    }

    private JahiaLDAPGroup lookupGroupInLDAP (int siteID, String name) {
        JahiaLDAPGroup group = lookupGroupInLDAP (name);
        if (group == null) {
            return null;
        }
        group.setSiteID (siteID);
        return group;
    }

    /**
     * Remove the specified user from all the membership lists of all the groups.
     *
     * @param user Reference on an existing user.
     *
     * @return Return true on success, or false on any failure.
     */

    public synchronized boolean removeUserFromAllGroups (JahiaUser user) {
        if (user != null && PROVIDER_NAME.equals(user.getProviderName())) {
            return false;
        }
        return true;
    }

    /**
     * Find groups according to a table of name=value properties. If the left
     * side value is "*" for a property then it will be tested against all the
     * properties. ie *=test* will match every property that starts with "test"
     *
     * @param siteID          site identifier
     * @param searchCriterias a Properties object that contains search criterias
     *                        in the format name,value (for example "*"="*" or "groupname"="*test*") or
     *                        null to search without criterias
     *
     * @return Set a set of JahiaGroup elements that correspond to those
     *         search criterias, or an empty one if an error has occured. Note this will
     *         only return the configured limit of groups at maxium. Check out the
     *         groups.ldap.properties file to change the limit.
     */
    public Set<JahiaGroup> searchGroups (int siteID, Properties searchCriterias) {
        Set<JahiaGroup> result = new HashSet<JahiaGroup>();

        // first let's lookup the user by the properties in Jahia's DB
        Set<String> groupKeys = searchLDAPGroupsByDBProperties(siteID, searchCriterias);
        // now that we have the keys, let's load all the users.
        Iterator<String> groupKeyEnum = groupKeys.iterator();
        while (groupKeyEnum.hasNext()) {
            String curGroupKey = groupKeyEnum.next();
            JahiaGroup group = lookupGroup(curGroupKey);
            result.add(group);
        }

        // now we can search in the LDAP.
        DirContext ctx = null;
        try {
            ctx = getPublicContext ();
            for (SearchResult sr : getGroups (ctx, searchCriterias)) {
                JahiaLDAPGroup group = ldapToJahiaGroup (sr);
                if (group != null) {
                    result.add (group);
                }
            }
        } catch (SizeLimitExceededException slee) {
            logger.debug ("Search generated more than configured maximum search limit in, limiting to " +
                    this.ldapProperties.getProperty (SEARCH_COUNT_LIMIT_PROP) +
                    " first results...");
        } catch (PartialResultException pre) {
            logger.warn (pre);
        } catch (NamingException ne) {
            logger.warn ("JNDI warning",ne);
            result = new HashSet<JahiaGroup>();
        } finally {
            invalidateCtx(ctx);
        }
        return result;
    }

    private synchronized Set<String> searchLDAPGroupsByDBProperties (int siteID,
        Properties searchCriterias) {
        Set<String> groupKeys = new HashSet<String>();

        if (searchCriterias == null) {
            searchCriterias = new Properties();
            searchCriterias.setProperty("*", "*");
        }
        //todo implement it with jcr properties ..

        return groupKeys;
    }

    /**
     * Lookup the group information from the underlaying system (DB, LDAP, ... )
     * Try to lookup the group into the cache, if it's not in the cache, then
     * load it into the cahce from the database.
     *
     * EP : 2004/23/07 : big refactoring
     *
     * @param groupKey Group's unique identification key.
     *
     * @return Return a reference on a the specified group name. Return null
     *         if the group doesn't exist or when any error occured.
     */
    public JahiaGroup lookupGroup(String groupKey) {
        // String tmpGroupKey = removeKeyPrefix (groupKey);

        /*
         * 2004-16-06 : update by EP new cache to browse : cross providers ...
         */
        JahiaGroup group = mProvidersGroupCache.get("k" + groupKey);
        if (group == null) {
            // logger.debug(" group with key=" + tmpGroupKey + " is not found in cache");
            group = lookupGroupInLDAP(removeKeyPrefix(groupKey));

            if (group != null) {
                /*
                 * 2004-16-06 : update by EP new cache to populate : cross providers ...
                 */
                mProvidersGroupCache.put("k" + groupKey, group);
                // with name for speed
                mProvidersGroupCache.put("n" + group.getSiteID() + "_"
                        + group.getGroupname(), group);
                // 2004-23-07 : store wrappers
            }
        }
        return group;
    }

    private JahiaLDAPGroup lookupGroupInLDAP (String groupKey) {
        JahiaLDAPGroup group = null;
        Iterator<String> groupEnum = nonExistentGroups.iterator();

        logger.debug("lookupGroupInLDAP :: " + groupKey);

        //FIXME: lousy solution for avoiding the over-querying of the ldap for non-existent groups...
        while (groupEnum.hasNext ()) {
            if (groupKey.indexOf (groupEnum.next () + ":") != -1)
                return group;
        }
        //
        DirContext ctx = null;
        try {
            ctx = getPublicContext ();
            SearchResult sr = getPublicGroup (ctx, groupKey);
            if (sr == null) {
                return null;
            }
            group = ldapToJahiaGroup (sr);
        } catch (SizeLimitExceededException slee) {
            logger.warn ("Search generated more than configured maximum search limit, limiting to " +
                    this.ldapProperties.getProperty (SEARCH_COUNT_LIMIT_PROP) +
                    " first results...");
            group = null;
        } catch (PartialResultException pre) {
            logger.warn (pre);
        } catch (NamingException ne) {
            logger.warn ("JNDI warning",ne);
            group = null;
        } finally {
            invalidateCtx(ctx);
        }
        return group;
    }

    /**
     * Retrieves a group from the LDAP public repository.
     *
     * @param ctx the current context in which to search for the group
     * @param cn  the unique identifier for the group
     *
     * @return a SearchResult object, which is the *first* result matching the
     *         cn
     *
     * @throws NamingException
     */
    private SearchResult getPublicGroup (DirContext ctx, String cn)
            throws NamingException {
        Properties filters = new Properties ();

        filters.setProperty (ldapProperties.getProperty (SEARCH_ATTRIBUTE_PROP),
                cn);
        List<SearchResult> answer = getGroups (ctx, filters);
        SearchResult sr = null;
        if (!answer.isEmpty()) {
            // we only take the first value if there are multiple answers, which
            // should normally NOT happend if the groupKey is unique !!
            sr = answer.get(0);

            if (answer.size() > 1) {
                // there is at least a second result.
                // throw new NamingException("GroupLDAPService.getPublicGroup>" +
                //                           "Warning : multiple group with same groupKey in LDAP repository.");
                logger.info ("Warning : multiple groups with same CN in LDAP repository.");
            }
        }
        return sr;
    }

    public void updateCache(JahiaGroup jahiaGroup) {
        mProvidersGroupCache.put ("k"+jahiaGroup.getGroupKey(), jahiaGroup);
        mProvidersGroupCache.put ("n"+jahiaGroup.getSiteID()+"_"+jahiaGroup.getGroupname(), jahiaGroup);
    }
    
}
