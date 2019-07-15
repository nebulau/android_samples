package com.bytedance.android.lesson.restapi.restapi;

import com.bytedance.android.lesson.restapi.restapi.bean.Joke;
import com.bytedance.android.lesson.restapi.restapi.utils.NetworkUtils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testRequestWithURLConnection() {
//        String s = NetworkUtils.getResponseWithHttpURLConnection("https://www.android.com/");
        String s = NetworkUtils.getResponseWithHttpURLConnection("http://api.icndb.com/jokes/random");
        System.out.println(s);
    }

    @Test
    public void testRequestWithRetrofit() {
        Joke j = NetworkUtils.getResponseWithRetrofit();
        System.out.println(j.getValue().getJoke());
    }
}