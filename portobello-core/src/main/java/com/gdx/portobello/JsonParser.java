package com.gdx.portobello;

import static java.lang.Character.isDigit;
import static java.lang.String.format;
import static java.util.Arrays.copyOfRange;

import java.io.IOException;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class JsonParser
{
    private static final char[] NULL_CHARS = new char[] {'n', 'u', 'l', 'l'};
    private static final char[] TRUE_CHARS = new char[] {'t', 'r', 'u', 'e'};
    private static final char[] FALSE_CHARS = new char[] {'f', 'a', 'l', 's', 'e'};

    private final char[] chars;
    private int pos;

    public JsonParser(char[] chars)
    {
        this.chars = chars;
    }

    public Object parse() throws IOException
    {
        Object obj;
        try
        {
            obj = parseValue();
        }
        catch (IndexOutOfBoundsException e)
        {
            throw new IOException(format("Unexpected: <eof> @ %d", pos), e);
        }

        // Parse any remaining whitespace
        skipWhitespace();
        if (pos != chars.length)
        {
            throw new IOException("Excess data found");
        }
        if (!(obj instanceof Map) && !(obj instanceof List))
        {
            throw new IOException(format("Expected: Map or List, Found: %s", obj.getClass().getSimpleName()));
        }
        return obj;
    }

    private Object parseValue() throws IOException
    {
        skipWhitespace();
        switch (chars[pos])
        {
            case '{':
                consume();
                return parseObject();

            case '[':
                consume();
                return parseArray();

            case 'f':
                return parseFalse();

            case 't':
                return parseTrue();

            case 'n':
                return parseNull();

            case '"':
                return parseString();

            case '-':
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                return parseNumber();
            default:
                throw new IOException("Unknown value!");
        }
    }

    private Object parseObject() throws IOException
    {
        if (next('}')) return Collections.EMPTY_MAP;

        Map<CharSequence, Object> object = new HashMap<>();

        CharSequence key = parseString();
        need(':');
        object.put(key, parseValue());
        while (next(','))
        {
            key = parseString();
            need(':');
            object.put(key, parseValue());
        }
        need('}');
        return object;
    }

    private Object parseArray() throws IOException
    {
        if (next(']')) return Collections.EMPTY_LIST;

        Collection<Object> array = new ArrayList<>();
        array.add(parseValue());
        while(next(','))
        {
            array.add(parseValue());
        }
        need(']');
        return array;
    }

    private Number parseNumber() throws IOException
    {
        boolean isDecimal = false;
        int offset = pos;

        matchAny('-');
        if (matchAny('0'))
        {
            if (matchInRange('0', '9'))
            {
                throw new IOException("Bad number");
            }
        }
        else
        {
            matchDigits();
        }

        if (matchAny('.'))
        {
            isDecimal = true;
            matchDigits();
        }

        if (matchAny('e', 'E'))
        {
            matchAny('-', '+');
            matchDigits();
            return Float.parseFloat(new String(chars, offset, pos - offset));
        }
        else if(isDecimal)
        {
            return Double.parseDouble(new String(chars, offset, pos - offset));
        }
        else
        {
            return Long.parseLong(new String(chars, offset, pos - offset));
        }
    }

    private Object parseNull() throws IOException
    {
        if(!matchRun(NULL_CHARS))
        {
            throw new IOException(format("Expected <null>, Found: %s", new String(copyOfRange(chars, pos, 4))));
        }
        return null;
    }

    private Boolean parseTrue() throws IOException
    {
        if(!matchRun(TRUE_CHARS))
        {
            throw new IOException("Expected 'true', Found: " + new String(copyOfRange(chars, pos, 4)));
        }
        return Boolean.TRUE;
    }

    private Object parseFalse() throws IOException
    {
        if(!matchRun(FALSE_CHARS))
        {
            throw new IOException("Expected: 'false', Found: " + new String(copyOfRange(chars, pos, 5)));
        }
        return Boolean.FALSE;
    }

    private CharSequence parseString() throws IOException
    {
        need('"');
        int offset = pos;
        while (!matchAny('"'))
        {
            switch (chars[pos])
            {
                case '\\':
                    parseEscapedCharacter();
                    break;
                case '\b':
                case '\f':
                case '\n':
                case '\r':
                case '\t':
                    throw new IOException(format("Unescaped control character: %s", chars[pos]));
                default:
                    consume();
            }
        }
        return CharBuffer.wrap(chars, offset, pos - 1 - offset);
    }

    private void parseEscapedCharacter() throws IOException
    {
        consume();
        switch (chars[pos])
        {
            case '"':
            case '\\':
            case '/':
            case 'b':
            case 'f':
            case 'n':
            case 'r':
            case 't':
                consume();
                break;

            case 'u':
                consume();
                if(matchHexDigit() && matchHexDigit() && matchHexDigit() && matchHexDigit())
                {
                    break;
                }
                throw new IOException(format("Expected: 4 hex digits, Found: '%s'", new String(copyOfRange(chars, pos, pos+4))));

            default:
                throw new IOException(format("Expected: escape character or hex escape, Found: '%s'", chars[pos]));
        }
    }

    private void need(char c) throws IOException
    {
        if (!next(c))
        {
            throw new IOException(format("Expected: %s, Found: %s @ %d", c, chars[pos], pos));
        }
    }

    private boolean next(char c)
    {
        skipWhitespace();
        if (chars[pos] == c)
        {
            consume();
            return true;
        }
        return false;
    }

    private boolean matchRun(char[] chars)
    {
        for (int i = 0; i < chars.length; i++)
        {
            if(this.chars[pos] != chars[i])
            {
                return false;
            }
            consume();
        }
        return true;
    }

    private boolean matchAny(char...chars)
    {
        for (int i = 0; i < chars.length; i++)
        {
            if(this.chars[pos] == chars[i])
            {
                consume();
                return true;
            }
        }
        return false;
    }

    private boolean matchInRange(char low, char high)
    {
        if(chars[pos] >= low && chars[pos] <= high)
        {
            consume();
            return true;
        }
        return false;
    }

    private void skipWhitespace()
    {
        while (pos < chars.length && (chars[pos] == ' ' || chars[pos] == '\t' || chars[pos] == '\n' || chars[pos] == '\r'))
        {
            consume();
        }
    }

    private void matchDigits() throws IOException
    {
        if(!isDigit(chars[pos]))
        {
            throw new IOException("Expected: at least 1 digit, Found: <none> @ " + pos);
        }
        consume();
        while(isDigit(chars[pos]))
        {
            consume();
        }
    }

    private boolean matchHexDigit()
    {
        if(chars[pos] >= '0' && chars[pos] <= '9' ||
            chars[pos] >= 'a' && chars[pos] <= 'f' ||
            chars[pos] >= 'A' && chars[pos] <= 'F')
        {
            consume();
            return true;
        }
        return false;
    }

    private void consume()
    {
        pos++;
    }
}
