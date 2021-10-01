/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.ssp.transferobject;
 
import java.io.Serializable;
import java.util.UUID;

import org.jasig.ssp.model.EarlyAlert;
import org.jasig.ssp.model.StudentDocument;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
 
public class StudentDocumentTO extends AbstractAuditableTO<StudentDocument> implements TransferObject<StudentDocument>, Serializable {
	
	private static final long serialVersionUID = -1660022036026969566L;

    private CommonsMultipartFile file;
    
    private String comment;
    
    private String name;
    
    private UUID confidentialityLevelId;
    
    private String confidentialityLevelName;
    
    private String fileName;
    
    private String author;
 
    public StudentDocumentTO() {
    	super();
    }
    public StudentDocumentTO(StudentDocument studentDocument) {
    	this.setComment(studentDocument.getComments());
    	this.setConfidentialityLevelId(studentDocument.getConfidentialityLevel().getId());
    	this.setConfidentialityLevelName(studentDocument.getConfidentialityLevel().getName());
    	this.setFileName(studentDocument.getFileName());
    	this.setName(studentDocument.getName());
    	this.setAuthor(studentDocument.getAuthor().getFullName());
	}

	public CommonsMultipartFile getFile() {
        return file;
    }
 
    public void setFile(CommonsMultipartFile file) {
        this.file = file;
    }

	public UUID getConfidentialityLevelId() {
		return confidentialityLevelId;
	}

	public void setConfidentialityLevelId(UUID confidentialityLevelId) {
		this.confidentialityLevelId = confidentialityLevelId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}
	public String getConfidentialityLevelName() {
		return confidentialityLevelName;
	}
	public void setConfidentialityLevelName(String confidentialityLevelName) {
		this.confidentialityLevelName = confidentialityLevelName;
	}
}