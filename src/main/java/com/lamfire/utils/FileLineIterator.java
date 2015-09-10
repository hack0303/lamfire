package com.lamfire.utils;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Created with IntelliJ IDEA.
 * User: linfan
 * Date: 15-9-9
 * Time: 下午12:21
 * To change this template use File | Settings | File Templates.
 */
public class FileLineIterator implements Iterator<String> {
    private BufferedReader reader;
    private String line;

    public FileLineIterator(Reader input){
        if(input instanceof BufferedReader){
            reader = (BufferedReader)input;
        }else {
            reader = new BufferedReader(input);
        }
    }

    @Override
    public synchronized boolean hasNext() {
        if(line != null){
            return true;
        }
        try{
            line = reader.readLine();
            if(line != null){
                return true;
            }
        }catch (IOException e){

        }
        return false;
    }

    @Override
    public synchronized String next() {
        if(line == null){
            if(!hasNext()){
                throw new NoSuchElementException();
            }
        }
        String result = line;
        this.line = null;
        return result;
    }

    @Override
    public void remove() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
