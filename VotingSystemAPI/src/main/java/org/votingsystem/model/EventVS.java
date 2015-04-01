package org.votingsystem.model;

import org.votingsystem.util.DateUtils;
import org.votingsystem.util.EntityVS;
import org.votingsystem.util.TypeVS;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;
import java.util.logging.Logger;

import static javax.persistence.GenerationType.IDENTITY;

//import org.apache.solr.analysis.HTMLStripCharFilterFactory;
//import org.apache.solr.analysis.StandardTokenizerFactory;
/**
* License: https://github.com/votingsystem/votingsystem/wiki/Licencia
*/
//@Indexed
@Entity @Table(name="EventVS")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="eventVSType", discriminatorType=DiscriminatorType.STRING)
@DiscriminatorValue("EventVS")
/*@AnalyzerDef(name="eventVSAnalyzer",
	charFilters = { @CharFilterDef(factory = HTMLStripCharFilterFactory.class) }, 
	tokenizer =  @TokenizerDef(factory = StandardTokenizerFactory.class))*/
public class EventVS extends EntityVS implements Serializable {

    private static Logger log = Logger.getLogger(EventVS.class.getSimpleName());

    private static final long serialVersionUID = 1L;

    public enum Cardinality { EXCLUSIVE, MULTIPLE }

    public enum Type { CLAIM, ELECTION }

    public enum State { ACTIVE, TERMINATED, CANCELED, ERROR, PENDING, PENDING_SIGNATURE, DELETED_FROM_SYSTEM }


    @Id @GeneratedValue(strategy=IDENTITY) 
    @Column(name="id", unique=true, nullable=false)
    //@DocumentId
    private Long id;
    @Column(name="accessControlEventVSId", unique=true)
    private Long accessControlEventVSId;//id of the event in the Access Control
    @Column(name="content", columnDefinition="TEXT")
    //@Analyzer(definition = "customAnalyzer")
    private String content;
    @Column(name="metaInf", columnDefinition="TEXT")
    private String metaInf = "{}"; 
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="controlCenter")
    private ControlCenterVS controlCenterVS;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="accessControl")
    private AccessControlVS accessControlVS;
    @Column(name="subject", length=1000)
    //@Field(index = Index.YES, analyze=Analyze.YES, store=Store.YES)
    private String subject;
    @Enumerated(EnumType.STRING)
    @Column(name="state")
    //@Field(index = Index.YES, analyze=Analyze.YES, store=Store.YES)
    private State state;
    @Column(name="cardinality") @Enumerated(EnumType.STRING)
    private Cardinality cardinality = Cardinality.EXCLUSIVE;
    @OneToOne(mappedBy="eventVS")
    private KeyStoreVS keyStoreVS;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="userVS")
    private UserVS userVS;
    @Column(name="backupAvailable") private Boolean backupAvailable = Boolean.TRUE;
    //Owning Entity side of the relationship
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
    @JoinTable(name = "eventvs_tagvs", joinColumns = {
            @JoinColumn(name = "eventvs", referencedColumnName = "id", nullable = false) },
            inverseJoinColumns = { @JoinColumn(name = "tagvs", nullable = false, referencedColumnName = "id") })
    private Set<TagVS> tagVSSet;
    @Column(name="url")
    private String url;
    @OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy="eventVS")
    private Set<FieldEventVS> fieldsEventVS;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="dateBegin", length=23, nullable=false)
    //@Field(index = Index.NO, analyze=Analyze.NO, store = Store.YES)
    //@DateBridge(resolution = Resolution.HOUR)
    private Date dateBegin;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="dateFinish", length=23, nullable=false)
    //@Field(index = Index.NO, analyze=Analyze.NO, store = Store.YES)
    //@DateBridge(resolution = Resolution.HOUR)
    private Date dateFinish;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="dateCanceled", length=23)
    private Date dateCanceled;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="dateCreated", length=23)
    private Date dateCreated;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lastUpdated", length=23)
    private Date lastUpdated;

    @Transient private Type type = null;
    @Transient private String[] tags;
    @Transient private Integer numSignaturesCollected;
    @Transient private Integer numVotesCollected;
    @Transient private Boolean signed;
    @Transient private VoteVS voteVS;

    public EventVS() {}

    public EventVS(UserVS userVS, String subject, String content, Boolean backupAvailable, Cardinality cardinality,
                   Date dateFinish) {
        this.userVS = userVS;
        this.subject = subject;
        this.content = content;
        this.backupAvailable = backupAvailable;
        this.cardinality = cardinality;
        this.dateFinish = dateFinish;
    }

    public Type getType() {
        return type;
    }

    public EventVS setType(Type type) {
        this.type = type;
        return this;
    }

    public Integer getNumSignaturesCollected() {
        return numSignaturesCollected;
    }

    public void setNumSignaturesCollected(Integer numSignaturesCollected) {
        this.numSignaturesCollected = numSignaturesCollected;
    }

    public VoteVS getVoteVS() {
        return voteVS;
    }

    public void setVoteVS(VoteVS voteVS) {
        this.voteVS = voteVS;
    }

    public Integer getNumVotesCollected() {
        return numVotesCollected;
    }

    public void setNumVotesCollected(Integer numVotesCollected) {
        this.numVotesCollected = numVotesCollected;
    }

    public Set<FieldEventVS> getFieldsEventVS() {
        return fieldsEventVS;
    }

    public void setFieldsEventVS(Set<FieldEventVS> fieldsEventVS) {
        this.fieldsEventVS = fieldsEventVS;
    }

    public Cardinality getCardinality() {
        return cardinality;
    }

    public void setCardinality(Cardinality cardinality) {
        this.cardinality = cardinality;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public String getContent () {
        return content;
    }
    
    public void setContent (String content) {
        this.content = content;
    }

    public String getSubject() {
    	return subject;
    }
    
    public void setSubject (String subject) {
        this.subject = subject;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public void setUserVS(UserVS userVS) {
        this.userVS = userVS;
    }

    public UserVS getUserVS() {
        return userVS;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setDateBegin(Date dateBegin) {
        this.dateBegin = dateBegin;
    }

    public Date getDateBegin() {
        return dateBegin;
    }

    public void setDateFinish(Date dateFinish) { this.dateFinish = dateFinish; }

    public State getState() {
        return state;
    }

    public EventVS setState(State state) {
        this.state = state;
        return this;
    }

    public ControlCenterVS getControlCenterVS() {
        return controlCenterVS;
    }

    public void setControlCenterVS(ControlCenterVS controlCenterVS) {
        this.controlCenterVS = controlCenterVS;
    }

	public KeyStoreVS getKeyStoreVS() {
		return keyStoreVS;
	}

	public void setKeyStoreVS(KeyStoreVS keyStoreVS) {
		this.keyStoreVS = keyStoreVS;
	}

	public Boolean getBackupAvailable() { return backupAvailable; }

	public void setBackupAvailable(Boolean backupAvailable) {
		this.backupAvailable = backupAvailable;
	}

	public Set<TagVS> getTagVSSet() {
		return tagVSSet;
	}

	public void setTagVSSet(Set<TagVS> tagVSSet) {
		this.tagVSSet = tagVSSet;
	}

	public Date getDateCanceled() {
		return dateCanceled;
	}

	public void setDateCanceled(Date dateCanceled) {
		this.dateCanceled = dateCanceled;
	}

	public String getMetaInf() {
		return metaInf;
	}

	public EventVS setMetaInf(String metaInf) {
		this.metaInf = metaInf;
        return this;
	}

    public AccessControlVS getAccessControlVS() {
        return accessControlVS;
    }

    public void setAccessControlVS(AccessControlVS accessControlVS) {
        this.accessControlVS = accessControlVS;
    }

    public Long getAccessControlEventVSId() {
        return accessControlEventVSId;
    }

    public void setAccessControlEventVSId(Long accessControlEventVSId) {
        this.accessControlEventVSId = accessControlEventVSId;
    }

    public static String getURL(TypeVS type, String serverURL, Long id) {
        if(type == null) return serverURL + "/eventVS/" + id;
        switch (type) {
            case CLAIM_EVENT: return serverURL + "/eventVSClaim/" + id;
            case VOTING_EVENT: return serverURL + "/eventVSElection/" + id;
            default: return serverURL + "/eventVS/" + id;
        }
    }

    public String[] getTags() {
        return tags;
    }

    public Date getDateFinish() {
		if(dateCanceled != null) return dateCanceled;
		else return dateFinish;
	}

    public boolean isActive(Date selectedDate) {
        if(selectedDate == null) return false;
        boolean result = false;
        if (selectedDate.after(dateBegin) && selectedDate.before(dateFinish)) result = true;
        if(state != null && (state == State.CANCELED || state == State.DELETED_FROM_SYSTEM)) result = false;
        return result;
    }
    public Map getDataMap() {
        log.info("getDataMap");
        Map resultMap = new HashMap();
        resultMap.put("subject", subject);
        resultMap.put("content", content);
        resultMap.put("dateBegin", DateUtils.getDateStr(dateBegin));
        resultMap.put("dateFinish", DateUtils.getDateStr(dateFinish));
        if(url != null) resultMap.put("url", url);
        resultMap.put("backupAvailable", backupAvailable);
        if(userVS != null) {
            Map userVSHashMap = new HashMap();
            userVSHashMap.put("nif", userVS.getNif());
            userVSHashMap.put("email", userVS.getEmail());
            userVSHashMap.put("name", userVS.getName());
            resultMap.put("userVS", userVSHashMap);
        }
        if(voteVS != null) resultMap.put("voteVS", voteVS.getDataMap());
        if(accessControlEventVSId != null) resultMap.put("accessControlEventVSId", accessControlEventVSId);

        if (type != null) resultMap.put("type", type.toString());
        if (getAccessControlEventVSId() != null) resultMap.put("accessControlEventVSId", getAccessControlEventVSId());
        if (id != null) resultMap.put("id", id);
        resultMap.put("UUID", UUID.randomUUID().toString());
        if (tagVSSet != null && !tagVSSet.isEmpty()) {
            List<String> tagList = new ArrayList<String>();
            for (TagVS tag : tagVSSet) {
                tagList.add(tag.getName());
            }
            resultMap.put("tags", tagList);
        }
        if (controlCenterVS != null) {
            Map controlCenterMap = new HashMap();
            controlCenterMap.put("id", controlCenterVS.getId());
            controlCenterMap.put("name", controlCenterVS.getName());
            controlCenterMap.put("serverURL", controlCenterVS.getServerURL());
            resultMap.put("controlCenter", controlCenterMap);
        }
        if (getFieldsEventVS() != null && !getFieldsEventVS().isEmpty()) {
            List<Map> fieldList = new ArrayList<Map>();
            for (FieldEventVS opcion : getFieldsEventVS()) {
                Map field = new HashMap();
                field.put("content", opcion.getContent());
                field.put("value", opcion.getValue());
                field.put("id", opcion.getId());
                fieldList.add(field);
            }
            resultMap.put("fieldsEventVS", fieldList);
        }
        if (cardinality != null) resultMap.put("cardinality", cardinality.toString());
        return resultMap;
    }

    public static Type getType(EventVS eventVS) {
        if(eventVS instanceof EventVSClaim) return Type.CLAIM;
        if(eventVS instanceof EventVSElection) return Type.ELECTION;
        return null;
    }

    public Map getChangeEventDataMap(String serverURL, State state) {
        log.info("getCancelEventDataMap");
        Map resultMap = new HashMap();
        resultMap.put("operation", TypeVS.EVENT_CANCELLATION.toString());
        resultMap.put("accessControlURL", serverURL);
        if(getAccessControlEventVSId() != null) resultMap.put("eventId", getAccessControlEventVSId());
        else resultMap.put("eventId", id);
        resultMap.put("state", state.toString());
        resultMap.put("UUID", UUID.randomUUID().toString());
        return resultMap;
    }

    public static EventVS parse (Map eventMap) throws Exception {
        EventVS eventVS = new EventVS();
        if(eventMap.get("id") != null &&
                !"null".equals(eventMap.get("id").toString())) {
            eventVS.setId(((Integer) eventMap.get("id")).longValue());
        }
        if(eventMap.containsKey("accessControlEventVSId")) eventVS.setAccessControlEventVSId(
                ((Integer) eventMap.get("accessControlEventVSId")).longValue());
        if(eventMap.containsKey("URL")) eventVS.setUrl((String) eventMap.get("URL"));
        if(eventMap.containsKey("controlCenter")) {
            Map controlCenterMap = (Map) eventMap.get("controlCenter");
            controlCenterMap.put("serverType", ActorVS.Type.CONTROL_CENTER);
            eventVS.setControlCenterVS((ControlCenterVS) ActorVS.parse(controlCenterMap));
        }
        if(eventMap.containsKey("accessControl")) {
            Map accessControlMap = (Map) eventMap.get("accessControl");
            accessControlMap.put("serverType", ActorVS.Type.ACCESS_CONTROL);
            eventVS.setAccessControlVS((AccessControlVS) ActorVS.parse(accessControlMap));
        }
        if(eventMap.containsKey("subject")) eventVS.setSubject((String) eventMap.get("subject"));
        if(eventMap.containsKey("voteVS")) {
            VoteVS voteVS = VoteVS.parse((Map) eventMap.get("voteVS"));
            eventVS.setVoteVS(voteVS);
        }
        if(eventMap.containsKey("state")) {
            State state = State.valueOf((String) eventMap.get("state"));
            eventVS.setState(state);
        }
        if(eventMap.containsKey("content")) eventVS.setContent((String) eventMap.get("content"));
        if(eventMap.containsKey("dateBegin")) eventVS.setDateBegin(
                DateUtils.getDateFromString((String) eventMap.get("dateBegin")));
        if(eventMap.containsKey("dateFinish")) eventVS.setDateFinish(
                DateUtils.getDateFromString((String) eventMap.get("dateFinish")));
        if (eventMap.containsKey("numSignaturesCollected"))
            eventVS.setNumSignaturesCollected((Integer) eventMap.get("numSignaturesCollected"));
        if (eventMap.containsKey("numVotesCollected"))eventVS.setNumVotesCollected((Integer) eventMap.get("numVotesCollected"));
        if(eventMap.containsKey("cardinality")) {
            eventVS.setCardinality(Cardinality.valueOf((String) eventMap.get("cardinality")));
        }
        if (eventMap.containsKey("userVS")) {
            Object userVSData = eventMap.get("userVS");
            if(userVSData instanceof String) {
                UserVS userVS = new UserVS();
                userVS.setName((String) eventMap.get("userVS"));
            } else eventVS.setUserVS(UserVS.parse((Map) userVSData));
        }
        if (eventMap.containsKey("backupAvailable")) {
            eventVS.setBackupAvailable((Boolean) eventMap.get("backupAvailable"));
        }
        if(eventMap.containsKey("fieldsEventVS")) {
            Set<FieldEventVS> fieldsEventVS =  new HashSet<FieldEventVS>();
            Object fieldList =  eventMap.get("fieldsEventVS");
            for(Object option : (List) eventMap.get("fieldsEventVS")) {
                if(option instanceof Map) fieldsEventVS.add(FieldEventVS.parse((Map)option));
                else fieldsEventVS.add(new FieldEventVS(((String)option), null));
            }
            eventVS.setFieldsEventVS(fieldsEventVS);
        }
        if(eventMap.get("tags") != null && !"null".equals(eventMap.get("tags").toString())) {
            List<String> labelList = (List<String>)eventMap.get("tags");
            eventVS.setTags(labelList.toArray(new String[labelList.size()]));
        }
        return eventVS;
    }

}