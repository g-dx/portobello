package com.gdx.portobello;

import static java.lang.String.format;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;

public class JsonParserTest
{
    @Test
    public void shouldFail()
    {
        parseAndFail("fail1.json");
        parseAndFail("fail2.json");
        parseAndFail("fail3.json");
        parseAndFail("fail4.json");
        parseAndFail("fail5.json");
        parseAndFail("fail6.json");
        parseAndFail("fail7.json");
        parseAndFail("fail8.json");
        parseAndFail("fail9.json");
        parseAndFail("fail10.json");
        parseAndFail("fail11.json");
        parseAndFail("fail12.json");
        parseAndFail("fail13.json");
        parseAndFail("fail14.json");
        parseAndFail("fail15.json");
        parseAndFail("fail16.json");
        parseAndFail("fail17.json");
//        parseAndFail("fail18.json"); // TODO: Check there is a maximum nesting supported by JSON spec
        parseAndFail("fail19.json");
        parseAndFail("fail20.json");
        parseAndFail("fail21.json");
        parseAndFail("fail22.json");
        parseAndFail("fail23.json");
        parseAndFail("fail24.json");
        parseAndFail("fail25.json");
        parseAndFail("fail26.json");
        parseAndFail("fail27.json");
        parseAndFail("fail28.json");
        parseAndFail("fail29.json");
        parseAndFail("fail30.json");
        parseAndFail("fail31.json");
        parseAndFail("fail32.json");
        parseAndFail("fail33.json");
    }

    @Test
    public void shouldPass()
    {
        parse("pass1.json");
        parse("pass2.json");
        parse("pass3.json");
    }

    private static void parseAndFail(String resource)
    {
        parse(resource);
        throw new AssertionError("\nFile: " + resource + "\nReason: expected exception!");
    }

    private static void parse(String resource)
    {
        try
        {
            createParser(resource).parse();
        }
        catch (URISyntaxException e)
        {
            throw new AssertionError(format("Failed to load classpath resource: %s", resource));
        }
        catch (IOException e)
        {
            throw new AssertionError(format("%nFile: %s%nReason: %s", e.getMessage(), e));
        }
    }

    private static JsonParser createParser(String resource) throws URISyntaxException, IOException
    {
        URI uri = JsonParserTest.class.getClassLoader().getResource(resource).toURI();
        byte[] bytes = Files.readAllBytes(Paths.get(uri));
        return new JsonParser(new String(bytes, StandardCharsets.UTF_8).toCharArray());
    }
}
