package models.op;

import static java.lang.String.format;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;
import play.data.validation.Email;
import play.data.validation.MaxSize;
import play.data.validation.MinSize;
import play.data.validation.Password;
import play.data.validation.Unique;
import play.db.jpa.Model;
import play.libs.Codec;
import result.PolicyResult;
import utils.PlayUtil;

import com.ciaosir.client.ReturnCode;

import configs.TMConfigs;
import controllers.CRUD.Exclude;

@Entity(name = CPStaff.TABLE_NAME)
@JsonIgnoreProperties(value = {
        "navProp", "mananger", "password",
        "persistent", "entityId", "updated", "superadmin", "payed"
})
public class CPStaff extends Model implements Comparable<CPStaff> {

    private static final Logger log = LoggerFactory.getLogger(CPStaff.class);

    @Transient
    private static final String USERNAME_PATTERN = "[\\x21-\\x7e]{6,64}";

    @Transient
    private static final String PASSWORD_PATTERN = "[\\x21-\\x7e]{6,64}";

    @Transient
    private boolean isPasswordChanged = false;

    @Exclude
    @Transient
    public static final String TABLE_NAME = "cp_staff";

    @Exclude
    @Transient
    public static final CPStaff NULL = null;

    public static class TYPE {
        public static final int DX = 1;
    }

    public static class STATUS {
        public static final int NORMAL = 1;
    }

    public CPStaff() {
        super();
    }

    /**
     *  @author lzl
     *	SWEETY: 0     SUPERADMIN: 1     ADMIN: 2
     */
    public enum Role {
        SWEETY, // 员工
        SUPERADMIN, // 为所欲为
        ADMIN // SUPERADMIN的只读状态
    }

    public CPStaff(String name, String password, String phone, String email,
            int type, int status, Role role) {
        super();
        this.name = name;
        this.phone = phone;
        this.type = type;
        this.status = status;
        this.role = role;
        this.setPassword(password);
        this.created = System.currentTimeMillis();
    }

    @Unique
    @Index(name = "uname")
    @MinSize(value = 6)
    @MaxSize(value = 64)
    @Column(length = 64, unique = true, updatable = false)
    public String name;

    @Index(name = "parentName")
    @MinSize(value = 6)
    @MaxSize(value = 64)
    @Column(length = 64)
    public String parentName;
    
    @Password
    @MinSize(value = 6)
    @MaxSize(value = 64)
    @Column(length = 64)
    @JsonIgnore
    public String password;

    @Column(length = 18)
    @MaxSize(value = 18)
    public String phone = null;

    @JsonIgnore(value = true)
    @Exclude
    public int type = TYPE.DX;

    @JsonIgnore(value = true)
    @Exclude
    public int status = STATUS.NORMAL;

    public Role role;

    @Transient
    public long acceptNum;
    
    //@JsonIgnore
    public long created;

    public CPStaff(String name, String password) {
        this.name = name;
        this.setPassword(password);
        this.created = System.currentTimeMillis();
    }

    public CPStaff(String name, String password, String email, String phone) {
        this.name = name;
        this.setPassword(password);
        this.phone = phone;
        this.created = System.currentTimeMillis();
    }

    public CPStaff(String username, String name2, String email2, Role role2) {
        this.name = username;
        this.setPassword(name2);
        this.role = role2;
        this.created = System.currentTimeMillis();
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getParentName() {
		return parentName;
	}

	public void setParentName(String parentName) {
		this.parentName = parentName;
	}

    public long getAcceptNum() {
		return acceptNum;
	}

	public void setAcceptNum(long acceptNum) {
		this.acceptNum = acceptNum;
	}

	public String getUsername() {
        return this.name;
    }

    public static final CPStaff create(String name, String password) {
        return new CPStaff(name, password);
    }

    public static final CPStaff connect(String name, String password) {
        CPStaff user = CPStaff.find("name = ?", name).first();
        if (user == null) {
            return null;
        }

        if (StringUtils.isBlank(password)) {
            return null;
        }

        if (user.checkPassword(password)) {
            return user;
        } else {
            return null;
        }
    }

    public boolean isSuperAdmin() {
        if (this.role == Role.SUPERADMIN) {
            return true;
        } else {
            return false;
        }
    }
    
    public boolean isAdmin() {
        if (this.role == Role.SUPERADMIN || this.role == Role.ADMIN) {
            return true;
        } else {
            return false;
        }
    }

    public static String CPSTaffCache = "CPSTaffCache";
    public static CPStaff findByName(String name) {
    	if(StringUtils.isEmpty(name)) {
    		return null;
    	}
        log.info(format("findByName:name".replaceAll(", ", "=%s, ") + "=%s", name));
        CPStaff staff = (CPStaff) Cache.get(CPSTaffCache + name.trim());
        if(staff != null) {
        	return staff;
        }
        staff = find("byName", name).first();
        Cache.set(CPSTaffCache + name.trim(), staff, "2h");
        return staff;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public void _save() {
        if (!this.isPersistent() || isPasswordChanged) {
            this.password = genPassword(this.password);
        }

        log.info("[this : ]" + this);
        super._save();
    }

    private String genPassword(String pswd) {
        String passwordSalt = Codec.encodeBASE64(Codec.UUID()).substring(0, 16);
        String passwordHash = genPasswordHash(pswd, passwordSalt);
        return passwordSalt + passwordHash;
    }

    public void changePassword(String pswd) {
        if (!isPasswordValid(pswd)) {
            // throw new CredResult(ReturnCode.INVALID_PASSWORD);
        }

        this.isPasswordChanged = true;

        _save();
    }

    public CPStaff setPassword(String password) {
        this.isPasswordChanged = true;
        this.password = password;
        return this;
    }

    public CPStaff clearPassword() {
        this.password = StringUtils.EMPTY;
        return this;
    }

    public static boolean hasName(String name) {
        return CPStaff.find("name = ?", name).first() != null;
    }

    public String getPassword() {
        return password;
    }

    public boolean checkPassword(String targetPassword) {
        log.info("[This Password]" + this.password);
        String salt = this.password.substring(0, 16);
        String hash = this.password.substring(16);

        return StringUtils.equals(hash, genPasswordHash(targetPassword, salt));
    }

    public static final void validName(String name) {
        if (StringUtils.isEmpty(name) || !name.matches(USERNAME_PATTERN)) {
            throw new PolicyResult(ReturnCode.INVALID_NAME);
        }
    }

    public static final boolean isNameValid(String name) {
        if (StringUtils.isEmpty(name)) {
            return false;
        }
        return name.matches(USERNAME_PATTERN);
    }

    public static final void validPassword(String password) {
        if (StringUtils.isEmpty(password)
                || !password.matches(PASSWORD_PATTERN)) {
            throw new PolicyResult(ReturnCode.INVALID_PASSWORD);
        }
    }

    public static final boolean isPasswordValid(String password) {
        if (StringUtils.isEmpty(password)) {
            return false;
        }
        return password.matches(PASSWORD_PATTERN);
    }

    private static final String genPasswordHash(String src, String salt) {
        return Codec.hexSHA1(String.format("{%s} salt={%s}", src, salt));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    @Override
    public String toString() {
        return "User [name=" + name + ", password=******" + ", phone=" + phone
                + ", type=" + type + ", status=" + status
                + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        CPStaff other = (CPStaff) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

    @Override
    public int compareTo(CPStaff o) {
        if (o == null) {
            return 0;
        }
        return this.name.compareTo(o.name);
    }

}
