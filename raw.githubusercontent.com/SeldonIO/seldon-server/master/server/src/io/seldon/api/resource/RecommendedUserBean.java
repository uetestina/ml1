/*
 * Seldon -- open source prediction engine
 * =======================================
 *
 * Copyright 2011-2015 Seldon Technologies Ltd and Rummble Ltd (http://www.seldon.io/)
 *
 * ********************************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ********************************************************************************************
 */

package io.seldon.api.resource;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

/**
 * @author claudio
 */

@Component
public class RecommendedUserBean extends ResourceBean {

    String user;
	String clientUserId;
	Double score;
	List<String> items;
    List<String> reasons;
    List<String> reasonTranslations;

	public RecommendedUserBean() {}


    public RecommendedUserBean(String user) {
        this.user = user;
    }


	public RecommendedUserBean(String user, String clientUserId, Double score, List<String> items) {
		this.user = user;
		this.clientUserId = clientUserId;
		this.score = score;
		this.items = items;
		this.reasons = new ArrayList<>();
        this.reasonTranslations = new ArrayList<>();
	}


	public RecommendedUserBean(String user, String clientUserId, Double score, List<String> items, List<String> reasons) {
		super();
		this.user = user;
		this.clientUserId = clientUserId;
		this.score = score;
		this.items = items;
		this.reasons = reasons;
	}


	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public Double getScore() {
		return score;
	}

	public void setScore(Double score) {
		this.score = score;
	}

	public List<String> getItems() {
		return items;
	}
	
	public void setItems(List<String> items) {
		this.items = items;
	}
	
	public String getClientUserId() {
        return clientUserId;
    }

    public void setClientUserId(String clientUserId) {
        this.clientUserId = clientUserId;
    }

    public List<String> getReasons() {
		return reasons;
	}

	public void setReasons(List<String> reasons) {
		this.reasons = reasons;
	}

    public List<String> getReasonTranslations() {
        return reasonTranslations;
    }

    public void setReasonTranslations(List<String> reasonTranslations) {
        this.reasonTranslations = reasonTranslations;
    }



	@Override
	public String toKey() {
		return user;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RecommendedUserBean that = (RecommendedUserBean) o;

        if (user != null ? !user.equals(that.user) : that.user != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return user != null ? user.hashCode() : 0;
    }
}
