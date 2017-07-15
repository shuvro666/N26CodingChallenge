package com.rest;

import java.util.Iterator;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.objects.Statistics;
import com.objects.Transaction;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Path("/api")
public class Transactions {

	private static final int SECONDS_STAT = 60;

	private static final ConcurrentMap<Long, Double> resource = new ConcurrentHashMap<Long, Double>();

	@GET
	@Path("/statistics")
	@Produces(MediaType.APPLICATION_JSON)
	public Statistics getStatistics() {
		System.out.println("GET");

		long count = 0;
		double sum = 0;
		double avg;
		double max = 0;
		double min = 0;
		System.out.println(Transactions.resource.size());

		Iterator resourceIter = Transactions.resource.keySet().iterator();
		while (resourceIter.hasNext()) {
			Long key = (Long) resourceIter.next();
			double amount = Transactions.resource.get(key);
			System.out.println(System.currentTimeMillis());
			System.out.println(key);
			if ((System.currentTimeMillis() - key) / 1000 < SECONDS_STAT) {
				count += 1;
				if (count == 1) {
					min = amount;
					max = amount;
					sum = amount;
				} else {
					if (amount < min) {
						min = amount;
					} else if (amount < max) {
						max = amount;
					}
					sum += amount;
				}
			}
		}
		avg = sum / count;

		Statistics statistics = new Statistics();
		statistics.setAvg(avg);
		statistics.setMax(max);
		statistics.setCount(count);
		statistics.setMin(min);
		statistics.setSum(sum);

		return statistics;

	}

	@POST
	@Path("/transactions")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createTrackInJSON(Transaction transaction) {
		System.out.println("POST");
		System.out.println(transaction.getAmount());
		System.out.println(transaction.getTimestamp());
		Transactions.resource.put(transaction.getTimestamp(), transaction.getAmount());

		int statusCode = 0;
		String statusMessage = "";

		if ((System.currentTimeMillis() - transaction.getTimestamp()) / 1000 < SECONDS_STAT) {
			statusCode = 201;
			statusMessage = "SUCCESS";
		} else {
			statusCode = 204;
			statusMessage = "TRANSACTION IS OLDER THAN 60s";
		}

		return Response.status(statusCode).entity(statusMessage).build();
	}

}
