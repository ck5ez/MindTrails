package edu.virginia.psyc.pi.persistence;

import edu.virginia.psyc.pi.domain.Participant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: dan
 * Date: 3/26/14
 * Time: 10:41 PM
 * This is where the login information for participants is stored.
 * By implementing the UserDetails interface we are able to use this directly
 * to lookup and authenticate users within the Spring Security Framework.
 *
 */
@Entity
@Table(name="participant")
public class ParticipantDAO implements UserDetails {

    private static final Logger LOG = LoggerFactory.getLogger(ParticipantDAO.class);

    @Id
    @GeneratedValue
    private long id;
    private String fullName;

    @Column(unique=true)
    private String email;

    private String password;

    private boolean admin;

    private boolean emailOptout;

    private boolean active;

    private boolean increase30;
    private boolean increase50;

    private Date          lastLoginDate;

    private Date          lastSessionDate;

    private String        randomToken;

    private String theme;

    private boolean over18;

    private String reference;

    private String riskSession;

    private boolean receiveGiftCards;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private PasswordTokenDAO  passwordTokenDAO;

    @Enumerated(EnumType.STRING)
    private Participant.PRIME prime;

    @Enumerated(EnumType.STRING)
    private Participant.CBM_CONDITION cbmCondition;

    private String currentSession; // set to first session by default

    private int taskIndex = 0;

    // IMPORTANT: Automatic email notifications start failing when
    // these relationships are setup with a FetchType.LAZY. Please
    // leave this eager, or address that problem.
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "participantDAO")
    @OrderBy("dateSent DESC")
    private SortedSet<EmailLogDAO> emailLogDAOs = new TreeSet<EmailLogDAO>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "participantDAO")
    @OrderBy("dateSent DESC")
    private SortedSet<GiftLogDAO> giftLogDAOs = new TreeSet<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "participantDAO")
    @OrderBy("dateCompleted DESC")
    private SortedSet<TaskLogDAO> taskLogDAOs = new TreeSet<>();


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> list = new ArrayList();
        list.add(new SimpleGrantedAuthority("ROLE_USER"));
        if(admin) list.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        return list;
    }

    // Default no-arg constructor
    public ParticipantDAO() {}

    // Utility to make testing easier.
    public ParticipantDAO(String fullName, String email, String password, boolean admin, String theme) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.admin = admin;
        this.theme = theme;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public String getCurrentSession() {
        return currentSession;
    }

    public void setCurrentSession(String currentSession) {
        this.currentSession = currentSession;
    }

    public int getTaskIndex() {
        return taskIndex;
    }

    public void setTaskIndex(int taskIndex) {
        this.taskIndex = taskIndex;
    }

    public boolean isEmailOptout() {
        return emailOptout;
    }

    public void setEmailOptout(boolean emailOptout) {
        this.emailOptout = emailOptout;
    }

    public Collection<EmailLogDAO> getEmailLogDAOs() {
        return emailLogDAOs;
    }

    public void setEmailLogDAOs(SortedSet<EmailLogDAO> emailLogDAOs) {
        this.emailLogDAOs = emailLogDAOs;
    }

    public void addEmailLog(EmailLogDAO log) {
        if(this.emailLogDAOs == null) this.emailLogDAOs = new TreeSet<>();
        this.emailLogDAOs.add(log);
    }

    public Collection<GiftLogDAO> getGiftLogDAOs() {
        return giftLogDAOs;
    }

    public void setGiftLogDAOs(SortedSet<GiftLogDAO> giftLogDAOs) {
        this.giftLogDAOs = giftLogDAOs;
    }

    public void addGiftLog(GiftLogDAO log) {
        if(this.giftLogDAOs == null) this.giftLogDAOs = new TreeSet<>();
        this.giftLogDAOs.add(log);
    }

    public Collection<TaskLogDAO> getTaskLogDAOs() {
        return taskLogDAOs;
    }

    public void setTaskLogDAOs(SortedSet<TaskLogDAO> taskLogDAOs) {
        this.taskLogDAOs = taskLogDAOs;
    }

    public void addTaskLog(TaskLogDAO log) {
        if(this.taskLogDAOs == null) this.taskLogDAOs = new TreeSet<>();
        this.taskLogDAOs.add(log);
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Date getLastLoginDate() {
        return lastLoginDate;
    }

    public void setLastLoginDate(Date lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    public Date getLastSessionDate() {
        return lastSessionDate;
    }

    public void setLastSessionDate(Date lastSessionDate) {
        this.lastSessionDate = lastSessionDate;
    }

    public PasswordTokenDAO getPasswordTokenDAO() {
        return passwordTokenDAO;
    }

    public void setPasswordTokenDAO(PasswordTokenDAO passwordTokenDAO) {
        this.passwordTokenDAO = passwordTokenDAO;
    }

    public Participant.PRIME getPrime() {
        return prime;
    }

    public void setPrime(Participant.PRIME prime) {
        this.prime = prime;
    }

    public Participant.CBM_CONDITION getCbmCondition() {
        return cbmCondition;
    }

    public void setCbmCondition(Participant.CBM_CONDITION cbmCondition) {
        this.cbmCondition = cbmCondition;
    }


    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public boolean isOver18() {
        return over18;
    }

    public void setOver18(boolean over18) {
        this.over18 = over18;
    }

    public String getRiskSession() {return riskSession;}

    public void setRiskSession(String riskSession) {this.riskSession = riskSession;}

    public boolean isIncrease30() {return increase30;}

    public void setIncrease30(boolean increase30) {this.increase30 = increase30;}

    public boolean isIncrease50() {  return increase50; }

    public void setIncrease50(boolean increase50) {this.increase50 = increase50;}

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public boolean isReceiveGiftCards() {
        return receiveGiftCards;
    }

    public void setReceiveGiftCards(boolean receiveGiftCards) {
        this.receiveGiftCards = receiveGiftCards;
    }

    @Override
    public String toString() {
        return "ParticipantDAO{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", admin=" + admin +
                ", emailOptout=" + emailOptout +
                ", currentSession=" + currentSession +
                ", taskIndex=" + taskIndex +
                ", active=" + active +
                '}';
    }


}
