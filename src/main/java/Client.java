import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;
class Client {
    public static void main(String[] args) {
        try (Socket clientSocket = new Socket("localhost", 8989)) // этой строкой мы запрашиваем у сервера доступ на соединение
        {
            try (
                 BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); // поток чтения из сокета
                 BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())) // поток записи в сокет
            ) {
                System.out.println("Введите слово для поиска");
                Scanner scanner = new Scanner(System.in);
                String word = scanner.nextLine();

                out.write(word + "\n");
                out.flush();

                System.out.println(in.readLine());
            } catch (IOException e) {
                System.err.println(e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}