package _epml;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class _Test_EpmlTokenizer {

	public static void main(String[] agrs) throws Exception {
		String epml="";
		List<String> list = Files.readAllLines(Paths.get("epml.txt"), Charset.forName("MS932"));
		epml = list.stream().collect(Collectors.joining("\n"));
		
		List<EpmlToken> tkns = new ArrayList<>();
		
		EpmlTokenizer etz = new EpmlTokenizer(epml);
		EpmlToken token;
		while((token = etz.getToken()) != null) {
			System.out.println(token);
			tkns.add(token);
			
		}
		
		System.out.println("----");
		
		tkns.stream().forEach(t->{
				if(t.getType()==NoteType.TEXT) {
					System.out.print(t.getValue());
				}else {
					System.out.print(t.getNote());
				}
			
			});

	}
}
