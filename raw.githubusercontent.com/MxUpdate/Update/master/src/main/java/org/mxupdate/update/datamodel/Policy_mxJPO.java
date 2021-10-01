/*
 *  This file is part of MxUpdate <http://www.mxupdate.org>.
 *
 *  MxUpdate is a deployment tool for a PLM platform to handle
 *  administration objects as single update files (configuration item).
 *
 *  Copyright (C) 2008-2016 The MxUpdate Team - All Rights Reserved
 *
 *  You may use, distribute and modify MxUpdate under the terms of the
 *  MxUpdate license. You should have received a copy of the MxUpdate
 *  license with this file. If not, please write to <info@mxupdate.org>,
 *  or visit <www.mxupdate.org>.
 *
 */

package org.mxupdate.update.datamodel;

import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;

import org.mxupdate.mapping.PropertyDef_mxJPO;
import org.mxupdate.typedef.EMxAdmin_mxJPO;
import org.mxupdate.update.AbstractAdminObject_mxJPO;
import org.mxupdate.update.datamodel.helper.AccessList_mxJPO;
import org.mxupdate.update.datamodel.helper.AccessList_mxJPO.Access;
import org.mxupdate.update.datamodel.helper.TriggerList_mxJPO;
import org.mxupdate.update.util.AbstractParser_mxJPO.ParseException;
import org.mxupdate.update.util.AdminPropertyList_mxJPO;
import org.mxupdate.update.util.AdminPropertyList_mxJPO.AdminProperty;
import org.mxupdate.update.util.CompareToUtil_mxJPO;
import org.mxupdate.update.util.DeltaUtil_mxJPO;
import org.mxupdate.update.util.ParameterCache_mxJPO;
import org.mxupdate.update.util.ParameterCache_mxJPO.ValueKeys;
import org.mxupdate.update.util.UpdateBuilder_mxJPO;
import org.mxupdate.update.util.UpdateBuilder_mxJPO.UpdateLine;
import org.mxupdate.update.util.UpdateException_mxJPO;
import org.mxupdate.update.util.UpdateException_mxJPO.ErrorKey;
import org.mxupdate.update.util.UpdateUtils_mxJPO;
import org.mxupdate.update.zparser.MxParser_mxJPO;
import org.mxupdate.util.MqlBuilderUtil_mxJPO;
import org.mxupdate.util.MqlBuilderUtil_mxJPO.MqlBuilder;
import org.mxupdate.util.MqlBuilderUtil_mxJPO.MultiLineMqlBuilder;
import org.mxupdate.util.StringUtils_mxJPO;

import matrix.util.MatrixException;

/**
 * The class is used to export and import / update policy configuration items.
 * The handled properties are:
 * <ul>
 * <li>package</li>
 * <li>uuid</li>
 * <li>symbolic names</li>
 * <li>description</li>
 * <li>hidden flag</li>
 * <li>{@link #formats} and {@link #defaultFormat default format}</li>
 * <li>{@link #enforce} flag</li>
 * <li>{@link #minorsequence minor} and {@link #majorsequence major sequence}
 *     with {@link #delimiter}</li>
 * <li>{@link #store}</li>
 * <li>{@link #types}</li>
 * <li>{@link #states}</li>
 * <li>properties</li>
 * </ul>
 *
 * @author The MxUpdate Team
 */
public class Policy_mxJPO
    extends AbstractAdminObject_mxJPO<Policy_mxJPO>
{
    /** Set of all ignored URLs from the XML definition for policies. */
    private static final Set<String> IGNORED_URLS = new HashSet<>();
    static  {
        Policy_mxJPO.IGNORED_URLS.add("/defaultFormat");
        Policy_mxJPO.IGNORED_URLS.add("/formatRefList");
        Policy_mxJPO.IGNORED_URLS.add("/typeRefList");
        Policy_mxJPO.IGNORED_URLS.add("/allstateDef/name");
        Policy_mxJPO.IGNORED_URLS.add("/allstateDef/ownerAccess/access");
        Policy_mxJPO.IGNORED_URLS.add("/allstateDef/ownerRevoke/access");
        Policy_mxJPO.IGNORED_URLS.add("/allstateDef/publicAccess/access");
        Policy_mxJPO.IGNORED_URLS.add("/allstateDef/publicRevoke/access");
        Policy_mxJPO.IGNORED_URLS.add("/allstateDef/userAccessList");
        Policy_mxJPO.IGNORED_URLS.add("/allstateDef/userAccessList/userAccess/access");
        Policy_mxJPO.IGNORED_URLS.add("/stateDefList");
        Policy_mxJPO.IGNORED_URLS.add("/stateDefList/stateDef/ownerAccess/access");
        Policy_mxJPO.IGNORED_URLS.add("/stateDefList/stateDef/publicAccess/access");
        Policy_mxJPO.IGNORED_URLS.add("/stateDefList/stateDef/ownerRevoke/access");
        Policy_mxJPO.IGNORED_URLS.add("/stateDefList/stateDef/publicRevoke/access");
        Policy_mxJPO.IGNORED_URLS.add("/stateDefList/stateDef/userAccessList");
        Policy_mxJPO.IGNORED_URLS.add("/stateDefList/stateDef/userAccessList/userAccess/access");
        Policy_mxJPO.IGNORED_URLS.add("/stateDefList/stateDef/signatureDefList");
        Policy_mxJPO.IGNORED_URLS.add("/stateDefList/stateDef/signatureDefList/signatureDef/approveUserList");
        Policy_mxJPO.IGNORED_URLS.add("/stateDefList/stateDef/signatureDefList/signatureDef/ignoreUserList");
        Policy_mxJPO.IGNORED_URLS.add("/stateDefList/stateDef/signatureDefList/signatureDef/rejectUserList");
        Policy_mxJPO.IGNORED_URLS.add("/stateDefList/stateDef/actionProgram");
        Policy_mxJPO.IGNORED_URLS.add("/stateDefList/stateDef/checkProgram");
        Policy_mxJPO.IGNORED_URLS.add("/stateDefList/stateDef/propertyList");
        Policy_mxJPO.IGNORED_URLS.add("/stateDefList/stateDef/routeUser");
        Policy_mxJPO.IGNORED_URLS.add("/stateDefList/stateDef/triggerList");
    }

    /** Default format of this policy. */
    private String defaultFormat = null;
    /** All possible formats of this policy. */
    private final SortedSet<String> formats = new TreeSet<>();
    /** Are all formats allowed of this policy? */
    private boolean allFormats = false;
    /** Locking enforced? */
    private boolean enforce;

    /** Delimiter between Major and Minor Revision. */
    private String delimiter = "";
    /** Minor Sequence of this policy. */
    private String minorsequence = null;
    /** Major Sequence of this policy. */
    private String majorsequence = null;

    /** Store of this policy. */
    private String store;

    /** Set of all types of this policy. */
    private final SortedSet<String> types = new TreeSet<>();
    /** Are all types allowed of this policy? */
    private boolean allTypes = false;

    /** Are access for all states defined? */
    private boolean allState = false;
    /** Access definitions for all states. */
    private final AccessList_mxJPO allStateAccess = new AccessList_mxJPO();

    /** Stack with all states of this policy. */
    private final Stack<State> states = new Stack<>();

    /**
     * Constructor used to initialize the type definition enumeration.
     *
     * @param _mxName   MX name of the policy object
     */
    public Policy_mxJPO(final String _mxName)
    {
        super(EMxAdmin_mxJPO.Policy, _mxName);
    }

    @Override
    public void parseUpdate(final String _code)
        throws SecurityException, IllegalArgumentException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, ParseException
    {
        new MxParser_mxJPO(new StringReader(_code)).parsePolicy(this);
        this.prepare();
    }

    /**
     * {@inheritDoc}
     * The {@link #allStateAccess all state access} and
     * {@link State#access state access} statements are sorted if defined.
     */
    @Override
    public void parse(final ParameterCache_mxJPO _paramCache)
        throws MatrixException, ParseException
    {
        super.parse(_paramCache);

        // define default values..
        if (this.minorsequence == null)  {
            this.minorsequence = "";
        }
        if (this.majorsequence == null)  {
            this.majorsequence = "";
        }

        if (_paramCache.getValueBoolean(ValueKeys.DMPolicyAllowExportAccessSorting))  {
            this.allStateAccess.sort();
            for (final State state : this.states)  {
                state.access.sort();
            }
        }
    }

    /**
     * Parses all policy specific URLs.
     *
     * @param _paramCache   parameter cache with MX context
     * @param _url          URL to parse
     * @param _content      content of the URL to parse
     * @return <i>true</i> if <code>_url</code> could be parsed; otherwise
     *         <i>false</i>
     * @see #IGNORED_URLS
     */
    @Override
    public boolean parseAdminXMLExportEvent(final ParameterCache_mxJPO _paramCache,
                                            final String _url,
                                            final String _content)
    {
        final boolean parsed;
        if (Policy_mxJPO.IGNORED_URLS.contains(_url))  {
            parsed = true;
        } else if ("/defaultFormat/formatRef".equals(_url))  {
            this.defaultFormat = _content;
            parsed = true;
        } else if ("/formatRefList/formatRef".equals(_url))  {
            this.formats.add(_content);
            parsed = true;
        } else if ("/allowAllFormats".equals(_url))  {
            this.allFormats = true;
            parsed = true;
        } else if ("/enforceLocking".equals(_url))  {
            this.enforce = true;
            parsed = true;

        } else if ("/delimiter".equals(_url))  {
            this.delimiter = _content;
            parsed = true;
        } else if ("/sequence".equals(_url))  {
            this.minorsequence = _content;
            parsed = true;
        } else if ("/majorsequence".equals(_url))  {
            this.majorsequence = _content;
            parsed = true;

        } else if ("/storeRef".equals(_url))  {
            this.store = _content;
            parsed = true;

        } else if ("/typeRefList/typeRef".equals(_url))  {
            this.types.add(_content);
            parsed = true;
        } else if ("/allowAllTypes".equals(_url))  {
            this.allTypes = true;
            parsed = true;

        } else if ("/allstateDef".equals(_url))  {
            this.allState = true;
            parsed = true;
        } else if (_url.startsWith("/allstateDef"))  {
            parsed = this.allStateAccess.parse(_paramCache, _url.substring(12), _content);

        } else if ("/stateDefList/stateDef".equals(_url))  {
            this.states.add(new State());
            parsed = true;
        } else if (_url.startsWith("/stateDefList/stateDef"))  {
            parsed = this.states.peek().parse(_paramCache, _url.substring(22), _content);

        } else  {
            parsed = super.parseAdminXMLExportEvent(_paramCache, _url, _content);
        }
        return parsed;
    }

    /**
     * Calls the {@link #allStateAccess all states} and single state prepare
     * methods. All policies properties starting with <code>state_</code> are
     * checked if they are defining a symbolic name of a state. If yes the
     * related symbolic name of the state is updated and removed from the
     * property list.
     */
    @Override
    protected void prepare()
    {
        for (final State state : this.states)  {
            state.prepare();
        }
        super.prepare();
        // extract state symbolic names from properties
        for (final AdminProperty property : new HashSet<>(this.getProperties().getProperties()))  {
            if ((property.getName() != null) && property.getName().startsWith("state_"))  {
                for (final State state : this.states)  {
                    if (state.name.equals(property.getValue()))  {
                        state.symbolicNames.add(property.getName());
                        this.getProperties().getProperties().remove(property);
                    }
                }
            }
        }
    }

    @Override
    public void writeUpdate(final UpdateBuilder_mxJPO _updateBuilder)
    {
        _updateBuilder
                //              tag             | default | value                                                                 | write?
                .stringNotNull( "package",                  this.getPackageRef())
                .stringNotNull( "uuid",                     this.getProperties().getValue4KeyValue(_updateBuilder.getParamCache(), PropertyDef_mxJPO.UUID))
                .list(          "symbolicname",             this.getSymbolicNames())
                .string(        "description",              this.getDescription())
                .flag(          "hidden",            false, this.isHidden())
                .singleIfTrue(  "type",                     "all",                                                                  this.allTypes)
                .listIfTrue(    "type",                     this.types,                                                             !this.allTypes)
                .singleIfTrue(  "format",                   "all",                                                                  this.allFormats)
                .listIfTrue(    "format",                   this.formats,                                                           !this.allFormats)
                .string(        "defaultformat",            this.defaultFormat)
                .flagIfTrue(    "enforce",           false, this.enforce,                                                           this.enforce)
                .stringIfTrue(  "delimiter",                this.delimiter,                                                         (this.delimiter != null) && !this.delimiter.isEmpty())
                .stringIfTrue(  "minorsequence",            this.minorsequence,                                                     (this.delimiter != null) && !this.delimiter.isEmpty())
                .stringIfTrue(  "majorsequence",            this.majorsequence,                                                     (this.delimiter != null) && !this.delimiter.isEmpty())
                .stringIfTrue(  "sequence",                 this.minorsequence,                                                     (this.delimiter == null) || this.delimiter.isEmpty())
                .string(        "store",                    this.store);

        // all state access
        if (this.allState)  {
            _updateBuilder.childStart("allstate");
            this.allStateAccess.write(_updateBuilder);
            _updateBuilder.childEnd();
        }

        // all states
        for (final State state : this.states)  {
            state.write(_updateBuilder);
        }

        _updateBuilder
                .properties(this.getProperties());
    }

    /**
     * Original method is overridden because policies must be directly created
     * within update.
     *
     * @param _paramCache       not used
     */
    @Override
    public void createOld(final ParameterCache_mxJPO _paramCache)
    {
    }

    @Override
    public void create(final ParameterCache_mxJPO _paramCache)
        throws MatrixException
    {
        final MqlBuilder mql = MqlBuilderUtil_mxJPO.mql().cmd("escape add policy ").arg(this.getName());
        if ((this.delimiter != null) && !this.delimiter.isEmpty())  {
            mql.cmd(" delimiter ").arg(this.delimiter)
               .cmd(" minorsequence ").arg(this.minorsequence)
               .cmd(" majorsequence ").arg(this.majorsequence);
        }
        mql.exec(_paramCache.getContext());
    }

    @Override
    public void calcDelta(final ParameterCache_mxJPO _paramCache,
                          final MultiLineMqlBuilder _mql,
                          final Policy_mxJPO _current)
        throws UpdateException_mxJPO
    {
        // check that delimiter is NOT updated
        if (((_current.delimiter == null) && (this.delimiter != null) && !this.delimiter.isEmpty())
                || ((_current.delimiter != null) && !_current.delimiter.isEmpty() && (this.delimiter == null))
                || ((_current.delimiter != null) && !_current.delimiter.equals(this.delimiter)))  {
            throw new UpdateException_mxJPO(
                    ErrorKey.DM_POLICY_UPDATE_DELIMITER,
                    this.getName(),
                    _current.delimiter,
                    this.delimiter);
        }

        DeltaUtil_mxJPO.calcPackage(_paramCache, _mql, this, _current);
        DeltaUtil_mxJPO.calcSymbNames(_paramCache, _mql, this, _current);

        DeltaUtil_mxJPO.calcValueDelta(_mql, "description",                  this.getDescription(),                      _current.getDescription());
        DeltaUtil_mxJPO.calcListDelta( _mql, "type",        this.allTypes,   this.types,            _current.allTypes,   _current.types);
        // if previous was defined all formats, but now not:
        // - format all must be removed
        // - to be sure, all other formats must be also removed...
        // - assign new defined formats
        if (!this.allFormats && _current.allFormats)  {
            _mql.newLine().cmd("remove format ").arg("all");
            for (final String format : _current.formats)  {
                _mql.newLine().cmd("remove format ").arg(format);
            }
            for (final String format : this.formats)  {
                _mql.newLine().cmd("add format ").arg(format);
            }
        } else  {
            DeltaUtil_mxJPO.calcListDelta( _mql, "format",  this.allFormats, this.formats,          _current.allFormats, _current.formats);
        }

        // if not default format => ADMINISTRATION must be default format
        if ((this.defaultFormat == null) || this.defaultFormat.isEmpty())  {
            if ((_current.defaultFormat != null) && !_current.defaultFormat.isEmpty())  {
                _mql.newLine().cmd("defaultformat ").arg("ADMINISTRATION");
            }
        } else  {
            DeltaUtil_mxJPO.calcValueDelta(_mql, "defaultformat", this.defaultFormat, _current.defaultFormat);
        }

        // enforce only if not equal
        if (_current.enforce != this.enforce)  {
            _mql.newLine();
            if (!this.enforce)  {
                _mql.cmd("not");
            }
            _mql.cmd("enforce");
        }

        DeltaUtil_mxJPO.calcValueDelta(_mql, "sequence", this.minorsequence, _current.minorsequence);
        if (_paramCache.getValueBoolean(ValueKeys.DMPolicySupportsMajorMinor))  {
            DeltaUtil_mxJPO.calcValueDelta(_mql, "majorsequence", this.majorsequence, _current.majorsequence);
        }

        // hidden flag, because hidden flag must be set with special syntax
        DeltaUtil_mxJPO.calcFlagDelta(_mql, "hidden", false, this.isHidden(), _current.isHidden());

        // if not default store => ADMINISTRATION must be default store
        if ((this.store == null) || this.store.isEmpty())  {
            if ((_current.store != null) && !_current.store.isEmpty())  {
                _mql.newLine().cmd("store ").arg("ADMINISTRATION");
            }
        } else  {
            DeltaUtil_mxJPO.calcValueDelta(_mql, "store", this.store, _current.store);
        }

        // all state access
        if (this.allState)  {
            if (!_current.allState)  {
                _mql.newLine().cmd("add allstate public none owner none");
            }

            _mql.pushPrefixByAppending("allstate");
            this.allStateAccess.calcDelta(_mql, (_current.allStateAccess != null) ? _current.allStateAccess : null);
            _mql.popPrefix();
        } else if (_current.allState)  {
            _mql.newLine().cmd("remove allstate");
        }

        // states delta
        _current.calcStatesDelta(_paramCache, _mql, this);
        // properties
        this.getProperties().calcDelta(_mql, "", _current.getProperties());
    }

    /**
     * Calculates the delta for states between this policy and given new policy
     * <code>_newPolicy</code>.
     *
     * @param _paramCache   parameter cache (used for logging purposes)
     * @param _mql          MQL builder for the MQL commands
     * @param _newPolicy    new target policy definition
     * @throws UpdateException_mxJPO if calculation of the delta failed
     */
    private void calcStatesDelta(final ParameterCache_mxJPO _paramCache,
                                 final MultiLineMqlBuilder _mql,
                                 final Policy_mxJPO _newPolicy)
        throws UpdateException_mxJPO
    {
        // states....
        // (first add new states because of references in branches)
        final Iterator<State> curStateIter = this.states.iterator();
        final Iterator<State> newStateIter = _newPolicy.states.iterator();
        final Map<State,State> stateDeltaMap = new HashMap<>();
        while (curStateIter.hasNext() && newStateIter.hasNext())  {
            final State curState = curStateIter.next();
            State newState = newStateIter.next();
            while (!curState.name.equals(newState.name) && newStateIter.hasNext())  {
                _mql.newLine()
                    .cmd("add state ").arg(newState.name).cmd(" before ").arg(curState.name).cmd(" public none owner none");
                _paramCache.logDebug("    - insert new state '" + newState.name + "' before '" + curState.name + "'");
                stateDeltaMap.put(newState, null);
                newState = newStateIter.next();
            }
            if (curState.name.equals(newState.name))  {
                stateDeltaMap.put(newState, curState);
            }
        }
        while (newStateIter.hasNext())  {
            final State newState = newStateIter.next();
            _mql.newLine()
                .cmd("add state ").arg(newState.name).cmd(" public none owner none");
            _paramCache.logDebug("    - add new state '" + newState.name + "'");
            stateDeltaMap.put(newState, null);
        }
        // check for already existing state, but not defined anymore!
        if (curStateIter.hasNext())  {
throw new UpdateException_mxJPO(null,"some states are not defined anymore!");
        }

        // now update state information itself
        for (final Entry<State, State> entry : stateDeltaMap.entrySet())  {
            _mql.pushPrefixByAppending("state $2", entry.getKey().name);
            entry.getKey().calcDelta(_paramCache, _mql, entry.getValue());
            _mql.popPrefix();
        }
        // symbolic names for the states must be appended at least
        // (so that state symbolic names can be moved from one state to another state)
        for (final Entry<State, State> entry : stateDeltaMap.entrySet())  {
            for (final String symbolicName : entry.getKey().symbolicNames)  {
                if ((entry.getValue() == null) || !entry.getValue().symbolicNames.contains(symbolicName))  {
                    _mql.newLine().cmd("add property ").arg(symbolicName).cmd(" value ").arg(entry.getKey().name);
                }
            }
        }
    }

    /**
     * Class defining states of a policy.
     */
    public static class State
    {
        /** Name of the state. */
        private String name;
        /** Symbolic Name of the state. */
        private final Set<String> symbolicNames = new TreeSet<>();

        /** Called action program for this state. */
        private String actionProgram = "";
        /** Input arguments for the action program for this state. */
        private String actionInput = "";

        /** Called check program for this state. */
        private String checkProgram = "";
        /** Input arguments for the check program for this state. */
        private String checkInput = "";

        /** Does the state have an auto promotion? */
        private boolean autoPromotion = false;

        /** Must a checkout written in the history? */
        private boolean checkoutHistory = false;

        /** Published flag? */
        private boolean published = false;

        /** Enoforcereserveaccess flag */
        private boolean enforcereserveaccess = false;

        /** Is the business object in this state (minor) revisionable? */
        private boolean minorrevisionable = false;
        /** Is the business object in this state major revisionable? */
        private boolean majorrevisionable = false;

        /** Is the business object in this state versionable? */
        private boolean versionable = false;

        /** Access definitions. */
        private final AccessList_mxJPO access = new AccessList_mxJPO();

        /** Route message of this state. */
        private String routeMessage;
        /** Route user of this state. */
        private String routeUser;

        /** Handles the state depending properties. */
        private final AdminPropertyList_mxJPO properties = new AdminPropertyList_mxJPO();

        /** Map with all triggers for this state. The key is the name of the trigger. */
        private final TriggerList_mxJPO triggers = new TriggerList_mxJPO();

        /** Holds the signatures for this state. */
        private final Stack<Signature> signatures = new Stack<>();

        /**
         * {@inheritDoc}
         */
        public boolean parse(final ParameterCache_mxJPO _paramCache,
                             final String _url,
                             final String _content)
        {
            boolean ret = true;
            if ("/name".equals(_url))  {
                this.name = _content;

            } else if ("/autoPromotion".equals(_url))  {
                this.autoPromotion = true;
            } else if ("/checkoutHistory".equals(_url))  {
                this.checkoutHistory = true;
            } else if ("/enforceReserveAccess".equals(_url))  {
                this.enforcereserveaccess = true;
            } else if ("/published".equals(_url))  {
                this.published = true;
            } else if ("/revisionable".equals(_url))  {
                this.minorrevisionable = true;
            } else if ("/majorrevisionable".equals(_url))  {
                this.majorrevisionable = true;
            } else if ("/versionable".equals(_url))  {
                this.versionable = true;

            } else if ("/signatureDefList/signatureDef".equals(_url))  {
                this.signatures.add(new Signature());
            } else if ("/signatureDefList/signatureDef/name".equals(_url))  {
                this.signatures.peek().name = _content;
            } else if ("/signatureDefList/signatureDef/expressionFilter".equals(_url))  {
                this.signatures.peek().filter= _content;
            } else if ("/signatureDefList/signatureDef/stateDefRef".equals(_url))  {
                this.signatures.peek().branch = _content;
            } else if ("/signatureDefList/signatureDef/approveUserList/userRef".equals(_url))  {
                this.signatures.peek().approverUsers.add(_content);
            } else if ("/signatureDefList/signatureDef/ignoreUserList/userRef".equals(_url))  {
                this.signatures.peek().ignoreUsers.add(_content);
            } else if ("/signatureDefList/signatureDef/rejectUserList/userRef".equals(_url))  {
                this.signatures.peek().rejectUsers.add(_content);

            } else if ("/actionProgram/programRef".equals(_url))  {
                this.actionProgram = _content;
            } else if ("/actionProgram/inputArguments".equals(_url))  {
                this.actionInput = _content;

            } else if ("/checkProgram/programRef".equals(_url))  {
                this.checkProgram = _content;
            } else if ("/checkProgram/inputArguments".equals(_url))  {
                this.checkInput = _content;

            } else if ("/routeMessage".equals(_url))  {
                this.routeMessage = _content;
            } else if ("/routeUser/userRef".equals(_url))  {
                this.routeUser = _content;

            } else if (_url.startsWith("/propertyList"))  {
                ret = this.properties.parse(_paramCache, _url.substring(13), _content);

            } else if (_url.startsWith("/triggerList"))  {
                ret = this.triggers.parse(_paramCache, _url.substring(12), _content);

            } else  {
                ret = this.access.parse(_paramCache, _url, _content);
            }
            return ret;
        }

        /**
         * <p>The trigger and property instances are sorted and the signatures
         * are fixed.</p>
         * <p><b>Hint for higher MX versions:</b>
         * <ul>
         * <li>filters are defined as filter from public none access with
         *     signature name as key</li>
         * <li>approve / ignore / reject users are defines as user access with
         *     approve / ignore / reject access and the signature name as
         *     key</li>
         * </ul></p>
         */
        protected void prepare()
        {
            // sort all triggers
            this.triggers.prepare();

            // sort the properties
            this.properties.prepare();

            // sort signatures
            final SortedSet<Signature> tmpSigs = new TreeSet<>(this.signatures);
            this.signatures.clear();
            this.signatures.addAll(tmpSigs);

            // fix filters of signature
            if (!this.signatures.isEmpty())  {
                // map between keys and access (key must be also equal signature)
                final Map<String,Access> filterMap = new HashMap<>();
                final Map<String,List<Access>> approveMap = new HashMap<>();
                final Map<String,List<Access>> ignoreMap = new HashMap<>();
                final Map<String,List<Access>> rejectMap = new HashMap<>();
                for (final Access oneAccess : this.access.getAccessList())  {
                    if ((oneAccess.getKey() != null) && !oneAccess.getKey().isEmpty() && oneAccess.getAccess().size() == 1)  {
                        final String accessStr = oneAccess.getAccess().iterator().next();
                        if ("public".equals(oneAccess.getKind()) && "none".equals(accessStr))  {
                            filterMap.put(oneAccess.getKey(), oneAccess);
                        } else if ("user".equals(oneAccess.getKind()) && "approve".equals(accessStr))  {
                            if (!approveMap.containsKey(oneAccess.getKey()))  {
                                approveMap.put(oneAccess.getKey(), new ArrayList<Access>());
                            }
                            approveMap.get(oneAccess.getKey()).add(oneAccess);
                        } else if ("user".equals(oneAccess.getKind()) && "ignore".equals(accessStr))  {
                            if (!ignoreMap.containsKey(oneAccess.getKey()))  {
                                ignoreMap.put(oneAccess.getKey(), new ArrayList<Access>());
                            }
                            ignoreMap.get(oneAccess.getKey()).add(oneAccess);
                        } else if ("user".equals(oneAccess.getKind()) && "reject".equals(accessStr))  {
                            if (!rejectMap.containsKey(oneAccess.getKey()))  {
                                rejectMap.put(oneAccess.getKey(), new ArrayList<Access>());
                            }
                            rejectMap.get(oneAccess.getKey()).add(oneAccess);
                        }
                    }
                }
                // signature filters are defined for public none filter
                for (final Signature signature : this.signatures)  {
                    // approve
                    if (approveMap.containsKey(signature.name))  {
                        for (final Access oneAccess : approveMap.get(signature.name))  {
                            signature.approverUsers.add(oneAccess.getUserRef());
                            this.access.getAccessList().remove(oneAccess);
                        }
                    }
                    // ignore
                    if (ignoreMap.containsKey(signature.name))  {
                        for (final Access oneAccess : ignoreMap.get(signature.name))  {
                            signature.ignoreUsers.add(oneAccess.getUserRef());
                            this.access.getAccessList().remove(oneAccess);
                        }
                    }
                    // reject
                    if (rejectMap.containsKey(signature.name))  {
                        for (final Access oneAccess : rejectMap.get(signature.name))  {
                            signature.rejectUsers.add(oneAccess.getUserRef());
                            this.access.getAccessList().remove(oneAccess);
                        }
                    }
                    // filter
                    if (filterMap.containsKey(signature.name))  {
                        final Access oneAccess = filterMap.get(signature.name);
                        signature.filter = oneAccess.getFilter();
                        this.access.getAccessList().remove(oneAccess);
                    }
                }
            }
        }

        /**
         * {@inheritDoc}
         * Further the complete information about the state is written:
         * <ul>
         * <li>{@link #name state name}
         * <li>{@link #symbolicNames symbolic name of the state}
         * <li>{@link #minorrevisionable minor is revisionable}
         * <li>{@link #majorrevisionable major is revisionable}
         * <li>{@link #versionable}
         * <li>{@link #autoPromotion auto promote}
         * <li>{@link #checkoutHistory}
         * <li>branches
         * <li>events
         * <li>triggers
         * <li>signatures
         * </ul>
         */
        public void write(final UpdateBuilder_mxJPO _updateBuilder)
        {
            _updateBuilder
                    .childStart("state \""+ UpdateUtils_mxJPO.encodeText(this.name) + "\"")
                    .list(          "registeredname",              this.symbolicNames)
                    .flagIfTrue(    "enforcereserveaccess", false, this.enforcereserveaccess,       _updateBuilder.getParamCache().getValueBoolean(ValueKeys.DMPolicyStateSupportsEnforceReserveAccess))
                    .flagIfTrue(    "majorrevision",        false, this.majorrevisionable,          _updateBuilder.getParamCache().getValueBoolean(ValueKeys.DMPolicySupportsMajorMinor))
                    .flagIfTrue(    "minorrevision",        false, this.minorrevisionable,          _updateBuilder.getParamCache().getValueBoolean(ValueKeys.DMPolicySupportsMajorMinor))
                    .flagIfTrue(    "revision",             false, this.minorrevisionable,          !_updateBuilder.getParamCache().getValueBoolean(ValueKeys.DMPolicySupportsMajorMinor))
                    .flag(          "version",              false, this.versionable)
                    .flag(          "promote",              false, this.autoPromotion)
                    .flag(          "checkouthistory",      false, this.checkoutHistory)
                    .flagIfTrue(    "published",            false, this.published,                  _updateBuilder.getParamCache().getValueBoolean(ValueKeys.DMPolicyStateSupportsPublished));
            // route
            if (!StringUtils_mxJPO.isEmpty(this.routeUser))  {
                _updateBuilder.stepStartNewLine().stepSingle("route").stepString(this.routeUser).stepString(this.routeMessage).stepEndLine();
            }

            this.access.write(_updateBuilder);

            if (((this.actionProgram != null) && !this.actionProgram.isEmpty()) || ((this.actionInput != null) && !this.actionInput.isEmpty()))  {
                _updateBuilder.stepStartNewLine().stepSingle("action").stepString(this.actionProgram).stepSingle("input").stepString(this.actionInput).stepEndLine();
            }
            if (((this.checkProgram != null) && !this.checkProgram.isEmpty()) || ((this.checkInput != null) && !this.checkInput.isEmpty()))  {
                _updateBuilder.stepStartNewLine().stepSingle("check").stepString(this.checkProgram).stepSingle("input").stepString(this.checkInput).stepEndLine();
            }

            _updateBuilder
                    .write(this.triggers)
                    .list(this.signatures)
                    .properties(this.properties)
                    .childEnd();
        }

        /**
         * Calculates the delta between this target state and current
         * {@code _oldState}.
         *
         * @param _paramCache   parameter cache (used for logging purposes)
         * @param _mql          MQL builder for the MQL commands
         * @param _oldState     old state to update ({@code null} if no old
         *                      state exists).
         */
        protected void calcDelta(final ParameterCache_mxJPO _paramCache,
                                 final MultiLineMqlBuilder _mql,
                                 final State _oldState)
        {
            // enforcereserveaccess flag (if supported)
            if (_paramCache.getValueBoolean(ValueKeys.DMPolicyStateSupportsEnforceReserveAccess))  {
                DeltaUtil_mxJPO.calcFlagDelta(_mql, "enforcereserveaccess", false, this.enforcereserveaccess, (_oldState != null) ? _oldState.enforcereserveaccess : null);
            }
            // basics
            DeltaUtil_mxJPO.calcValueDelta(_mql, "promote",         String.valueOf(this.autoPromotion),     (_oldState == null) ? null : String.valueOf(_oldState.autoPromotion));
            DeltaUtil_mxJPO.calcValueDelta(_mql, "checkoutHistory", String.valueOf(this.checkoutHistory),   (_oldState == null) ? null : String.valueOf(_oldState.checkoutHistory));
            DeltaUtil_mxJPO.calcValueDelta(_mql, "version",         String.valueOf(this.versionable),       (_oldState == null) ? null : String.valueOf(_oldState.versionable));
            if (_oldState == null || !this.actionProgram.equals(_oldState.actionProgram)|| !this.actionInput.equals(_oldState.actionInput))  {
                _mql.newLine()
                    .cmd("action ").arg(this.actionProgram).cmd(" input ").arg(this.actionInput);
            }
            if (_oldState == null || !this.checkProgram.equals(_oldState.checkProgram)|| !this.checkInput.equals(_oldState.checkInput))  {
                _mql.newLine()
                    .cmd("check ").arg(this.checkProgram).cmd(" input ").arg(this.checkInput);
            }
            // minor / major revision (if supported)
            if (_paramCache.getValueBoolean(ValueKeys.DMPolicySupportsMajorMinor))  {
                DeltaUtil_mxJPO.calcValueDelta(_mql, "majorrevision", String.valueOf(this.majorrevisionable), (_oldState == null) ? null : String.valueOf(_oldState.majorrevisionable));
                DeltaUtil_mxJPO.calcValueDelta(_mql, "minorrevision", String.valueOf(this.minorrevisionable), (_oldState == null) ? null : String.valueOf(_oldState.minorrevisionable));
            } else  {
                DeltaUtil_mxJPO.calcValueDelta(_mql, "revision", String.valueOf(this.minorrevisionable), (_oldState == null) ? null : String.valueOf(_oldState.minorrevisionable));
            }
            // published (if supported)
            if (_paramCache.getValueBoolean(ValueKeys.DMPolicyStateSupportsPublished))  {
                DeltaUtil_mxJPO.calcValueDelta(_mql, "published", String.valueOf(this.published), (_oldState == null) ? null : String.valueOf(_oldState.published));
            }
            // route user with message
            if (!StringUtils_mxJPO.isEmpty(this.routeUser) && ((_oldState == null) || !this.routeUser.equals(_oldState.routeUser)))  {
                _mql.newLine().cmd(" add route ").arg(this.routeUser);
            } else if (StringUtils_mxJPO.isEmpty(this.routeUser) && (_oldState != null) && !StringUtils_mxJPO.isEmpty(_oldState.routeUser))  {
                _mql.newLine().cmd(" remove route ");
            }
            if (!StringUtils_mxJPO.isEmpty(this.routeUser))  {
                DeltaUtil_mxJPO.calcValueDelta(_mql, "route message", this.routeMessage, (_oldState == null) ? null : String.valueOf(_oldState.routeMessage));
            }

            // access list
            this.access.calcDelta(_mql, (_oldState != null) ? _oldState.access : null);

            // triggers
            this.triggers.calcDelta(_mql, (_oldState != null) ? _oldState.triggers : null);

            // signatures
            final Set<String> newSigs = new HashSet<>();
            for (final Signature signature : this.signatures)  {
                newSigs.add(signature.name);
            }
            final Map<String,Signature> oldSigs = new HashMap<>();
            if (_oldState != null)  {
                for (final Signature signature : _oldState.signatures)  {
                    if (newSigs.contains(signature.name))  {
                        oldSigs.put(signature.name, signature);
                    } else  {
                        _mql.newLine()
                            .cmd("remove signature ").arg(signature.name);
                    }
                }
            }
            for (final Signature signature : this.signatures)  {
                final Signature oldSig;
                _mql.newLine();
                if ((_oldState != null) && !oldSigs.containsKey(signature.name))  {
                    _mql.cmd("add ");
                    oldSig = null;
                } else  {
                    oldSig = oldSigs.get(signature.name);
                }
                _mql.cmd("signature ").arg(signature.name);
                signature.calcDelta(_mql, oldSig);
            }

            // state properties
            this.properties.calcDelta(_mql, "state", (_oldState != null) ? _oldState.properties : null);

            // registered names (symbolic names)
            if (_oldState != null)  {
                for (final String symbolicName : _oldState.symbolicNames)  {
                    if (!this.symbolicNames.contains(symbolicName))  {
                        _mql.newLine().cmd("remove property ").arg(symbolicName);
                    }
                }
            }
        }
    }

    /**
     * Class defining a signature for a state.
     */
    public static class Signature
        implements UpdateLine, Comparable<Signature>
    {
        /** Name of the signature. */
        private String name;

        /** Branch to state? */
        private String branch;
        /** Expression filter of the signature. */
        private String filter;

        /** Set of users which could approve the signature. */
        private final Set<String> approverUsers = new TreeSet<>();
        /** Set of users which could ignore the signature. */
        private final Set<String> ignoreUsers = new TreeSet<>();
        /** Set of users which could reject the signature. */
        private final Set<String> rejectUsers = new TreeSet<>();

        /**
         * Appends the signature information including the branch, filter and
         * the approve, ignore and reject users.
         *
         * @param _updateBuilder    update builder
         */
        @Override
        public void write(final UpdateBuilder_mxJPO _updateBuilder)
        {
            _updateBuilder
                    .stepStartNewLine().stepSingle("signature").stepString(this.name).stepEndLineWithStartChild()
                    .string(        "branch",       this.branch)
                    .listOneLine(   "approve",      this.approverUsers)
                    .listOneLine(   "ignore",       this.ignoreUsers)
                    .listOneLine(   "reject",       this.rejectUsers)
                    .string(        "filter",       this.filter)
                    .childEnd();
        }

        /**
         * Calculates the delta between the old signature and this signature.
         * If for the old signature a branch is defined and for the new
         * signature no branch, an error is thrown.
         *
         * @param _mql          appendable instance where the delta must be
         *                      append
         * @param _oldSignature old signature to compare
         */
        protected void calcDelta(final MultiLineMqlBuilder _mql,
                                 final Signature _oldSignature)
        {
            if (this.branch.isEmpty())  {
                if ((_oldSignature != null) && (_oldSignature.branch != null) && !"".equals(_oldSignature.branch))  {
throw new Error("branch '" + _oldSignature.branch + "' exists for signature " + this.name + ", but is not defined anymore");
                }
            } else  {
                _mql.cmd(" branch ").arg(this.branch);
            }
            _mql.cmd(" filter ").arg(this.filter);
            // update approve users
            for (final String approver : this.approverUsers)  {
                if ((_oldSignature == null) || !_oldSignature.approverUsers.contains(approver))  {
                    _mql.cmd(" add approve ").arg(approver);
                }
            }
            if (_oldSignature != null)  {
                for (final String approver : _oldSignature.approverUsers)  {
                    if (!this.approverUsers.contains(approver))  {
                        _mql.cmd(" remove approve ").arg(approver);
                    }
                }
            }
            // update ignore user
            for (final String ignore : this.ignoreUsers)  {
                if ((_oldSignature == null) || !_oldSignature.ignoreUsers.contains(ignore))  {
                    _mql.cmd(" add ignore ").arg(ignore);
                }
            }
            if (_oldSignature != null)  {
                for (final String ignore : _oldSignature.ignoreUsers)  {
                    if (!this.ignoreUsers.contains(ignore))  {
                        _mql.cmd(" remove ignore ").arg(ignore);
                    }
                }
            }
            // update reject users
            for (final String reject : this.rejectUsers)  {
                if ((_oldSignature == null) || !_oldSignature.rejectUsers.contains(reject))  {
                    _mql.cmd(" add reject ").arg(reject);
                }
            }
            if (_oldSignature != null)  {
                for (final String reject : _oldSignature.rejectUsers)  {
                    if (!this.rejectUsers.contains(reject))  {
                        _mql.cmd(" remove reject ").arg(reject);
                    }
                }
            }
        }

        @Override
        public int compareTo(final Signature _compareTo)
        {
            int ret = 0;
            ret = CompareToUtil_mxJPO.compare(ret, this.name,           _compareTo.name);
            ret = CompareToUtil_mxJPO.compare(ret, this.branch,         _compareTo.branch);
            ret = CompareToUtil_mxJPO.compare(ret, this.filter,         _compareTo.filter);
            ret = CompareToUtil_mxJPO.compare(ret, this.approverUsers,  _compareTo.approverUsers);
            ret = CompareToUtil_mxJPO.compare(ret, this.ignoreUsers,    _compareTo.ignoreUsers);
            ret = CompareToUtil_mxJPO.compare(ret, this.rejectUsers,    _compareTo.rejectUsers);
            return ret;
        }
    }
}
