package org.kathrynhuxtable.acode.grammar.tree;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProcNode implements AcdNode {
	String name;
	List<String> args = new ArrayList<>();
	String code;
}
