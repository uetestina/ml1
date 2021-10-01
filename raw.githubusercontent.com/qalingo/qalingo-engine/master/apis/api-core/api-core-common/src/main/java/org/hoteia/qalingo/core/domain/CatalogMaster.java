/**
 * Most of the code in the Qalingo project is copyrighted Hoteia and licensed
 * under the Apache License Version 2.0 (release version 0.8.0)
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *                   Copyright (c) Hoteia, 2012-2014
 * http://www.hoteia.com - http://twitter.com/hoteia - contact@hoteia.com
 *
 */
package org.hoteia.qalingo.core.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import org.hibernate.Hibernate;
import org.hoteia.qalingo.core.domain.impl.DomainEntity;

@Entity
@Table(name="TECO_CATALOG_MASTER")
public class CatalogMaster extends AbstractCatalog<CatalogMaster, CatalogCategoryMaster> implements DomainEntity {

	/**
	 * Generated UID
	 */
	private static final long serialVersionUID = 5663387436039325641L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID", nullable = false)
    private Long id;

    @Version
    @Column(name = "VERSION", nullable = false) // , columnDefinition = "int(11) default 1"
    private int version;

    @Column(name = "CODE", unique = true, nullable = false)
    private String code;

    @Column(name = "NAME")
    private String name;

    @Column(name = "DESCRIPTION")
    @Lob
    private String description;
	
	@Column(name="IS_DEFAULT", nullable=false) // , columnDefinition="tinyint(1) default 0"
	private boolean isDefault = false;
	
//    @OneToMany(targetEntity = org.hoteia.qalingo.core.domain.CatalogCategoryMaster.class, fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
//    @JoinTable(name = "TECO_CATALOG_MASTER_CATEGORY_MASTER_REL", joinColumns = @JoinColumn(name = "MASTER_CATALOG_ID"), inverseJoinColumns = @JoinColumn(name = "MASTER_CATEGORY_ID"))
//    private Set<CatalogCategoryMaster> catalogCategories = new HashSet<CatalogCategoryMaster>();
	
    @OneToMany(fetch = FetchType.LAZY, cascade=CascadeType.ALL)
    @JoinColumn(name = "MASTER_CATALOG_ID")
    private Set<CatalogCategoryMaster> catalogCategories = new HashSet<CatalogCategoryMaster>();
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DATE_CREATE")
    private Date dateCreate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DATE_UPDATE")
    private Date dateUpdate;

	public CatalogMaster(){
        this.dateCreate = new Date();
        this.dateUpdate = new Date();
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}
	
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public boolean isDefault() {
		return isDefault;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public void setDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}

	public Set<CatalogCategoryMaster> getCatalogCategories() {
		return catalogCategories;
	}
	
    public List<CatalogCategoryMaster> getSortedAllCatalogCategories() {
        List<CatalogCategoryMaster> sortedCatalogCategories = null;
        if (catalogCategories != null 
                && Hibernate.isInitialized(catalogCategories)) {
            sortedCatalogCategories = new LinkedList<CatalogCategoryMaster>(catalogCategories);
            Collections.sort(sortedCatalogCategories, new Comparator<CatalogCategoryMaster>() {
                @Override
                public int compare(CatalogCategoryMaster o1, CatalogCategoryMaster o2) {
                    if (o1 != null && o1.getRanking() != null && o2 != null && o2.getRanking() != null) {
                        return o1.getRanking().compareTo(o2.getRanking());
                    }
                    return 0;
                }
            });
        }
        return sortedCatalogCategories;
    }
    
    public List<CatalogCategoryMaster> getSortedRootCatalogCategories() {
        List<CatalogCategoryMaster> rootCatalogCategories = null;
        List<CatalogCategoryMaster> sortedCatalogCategories = null;
        if (catalogCategories != null 
                && Hibernate.isInitialized(catalogCategories)) {
            rootCatalogCategories = new ArrayList<CatalogCategoryMaster>();
            for (Iterator<CatalogCategoryMaster> iterator = catalogCategories.iterator(); iterator.hasNext();) {
                CatalogCategoryMaster catalogCategoryMaster = (CatalogCategoryMaster) iterator.next();
                if(catalogCategoryMaster.isRoot()){
                    rootCatalogCategories.add(catalogCategoryMaster);
                }
            }
            sortedCatalogCategories = new LinkedList<CatalogCategoryMaster>(rootCatalogCategories);
            Collections.sort(sortedCatalogCategories, new Comparator<CatalogCategoryMaster>() {
                @Override
                public int compare(CatalogCategoryMaster o1, CatalogCategoryMaster o2) {
                    if (o1 != null && o1.getRanking() != null && o2 != null && o2.getRanking() != null) {
                        return o1.getRanking().compareTo(o2.getRanking());
                    }
                    return 0;
                }
            });
        }
        return sortedCatalogCategories;
    }
    
	public void setCatalogCategories(Set<CatalogCategoryMaster> catalogCategories) {
		this.catalogCategories = catalogCategories;
	}
	
	public Date getDateCreate() {
		return dateCreate;
	}

	public void setDateCreate(Date dateCreate) {
		this.dateCreate = dateCreate;
	}

	public Date getDateUpdate() {
		return dateUpdate;
	}

	public void setDateUpdate(Date dateUpdate) {
		this.dateUpdate = dateUpdate;
	}

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((code == null) ? 0 : code.hashCode());
        result = prime * result + ((dateCreate == null) ? 0 : dateCreate.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object sourceObj) {
        Object obj = deproxy(sourceObj);
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CatalogMaster other = (CatalogMaster) obj;
        if (code == null) {
            if (other.code != null)
                return false;
        } else if (!code.equals(other.code))
            return false;
        if (dateCreate == null) {
            if (other.dateCreate != null)
                return false;
        } else if (!dateCreate.equals(other.dateCreate))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "CatalogMaster [id=" + id + ", version=" + version + ", name=" + name + ", description=" + description + ", isDefault=" + isDefault + ", code=" + code + ", dateCreate="
                + dateCreate + ", dateUpdate=" + dateUpdate + "]";
    }
	
}