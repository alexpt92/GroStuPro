package SimiFinder;

import java.util.*;
import java.util.stream.Stream;

public class OrderOutput {
	public static <String, Integer extends Comparable<? super Integer>> Map<String, Integer> 
	sortMapByValues( Map<String, Integer> map )
{
    Map<String, Integer> result = new LinkedHashMap<>();
    Stream<Map.Entry<String, Integer>> st = map.entrySet().stream();

    st.sorted( Map.Entry.comparingByValue() )
        .forEachOrdered( e -> result.put(e.getKey(), e.getValue()) );

    return result;
}
}
