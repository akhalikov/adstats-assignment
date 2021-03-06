package com.adstats.ads;

import com.adstats.RestTestBase;
import com.adstats.service.model.Click;
import com.adstats.service.model.Delivery;
import com.datastax.driver.core.Row;
import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;
import java.time.ZonedDateTime;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.springframework.http.HttpStatus;

public class AdsControllerClickTest extends RestTestBase {

  @Test
  public void shouldAcceptValidRequest() {
    Delivery delivery = getTestDelivery(ZonedDateTime.now().minusSeconds(100));
    postOk("/delivery", delivery, Delivery.class);

    Click click = getTestClick(delivery.deliveryId, ZonedDateTime.now());
    postOk("/click", click, Click.class);

    Row result = cassandraSession.execute(select()
        .from("click")
        .where(eq("click_id", click.clickId)))
        .one();

    assertEquals(click.clickId, result.getString("click_id"));
    assertEquals(delivery.deliveryId, result.getString("delivery_id"));
  }

  @Test
  public void shouldReturn404IfDeliveryIsNotFound() {
    Click click = getTestClick("missing-delivery-id", ZonedDateTime.now());

    postAndExpect("/click", click, Click.class, HttpStatus.NOT_FOUND);
  }
}
