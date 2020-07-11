package com.example.agendaapp.Utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Serialize {

    public static void serialize(Object object, String fileName) {
        try {
            FileOutputStream file = new FileOutputStream(fileName);
            ObjectOutputStream outputStream = new ObjectOutputStream(file);

            outputStream.writeObject(object);

            outputStream.close();
            file.close();
        } catch(IOException e){}
    }

    public static Object deserialize(String fileName) {
        try {
            FileInputStream file = new FileInputStream(fileName);
            ObjectInputStream inputStream = new ObjectInputStream(file);

            Object returnObject = inputStream.readObject();

            inputStream.close();
            file.close();

            return returnObject;
        } catch(IOException|ClassNotFoundException e){}

        return null;
    }
}
