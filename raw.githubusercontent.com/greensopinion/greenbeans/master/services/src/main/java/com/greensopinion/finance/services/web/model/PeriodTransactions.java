/*******************************************************************************
 * Copyright (c) 2015, 2016 David Green.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.greensopinion.finance.services.web.model;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.greensopinion.finance.services.domain.Transaction;

public class PeriodTransactions {
	private final String name;
	private final List<TransactionModel> transactions;

	public PeriodTransactions(String name, List<Transaction> transactions) {
		this.name = checkNotNull(name);
		this.transactions = FluentIterable.from(transactions).transform(new Function<Transaction, TransactionModel>() {

			@Override
			public TransactionModel apply(Transaction transaction) {
				return new TransactionModel(transaction);
			}
		}).toList();
	}

	public String getName() {
		return name;
	}

	public List<TransactionModel> getTransactions() {
		return transactions;
	}
}
