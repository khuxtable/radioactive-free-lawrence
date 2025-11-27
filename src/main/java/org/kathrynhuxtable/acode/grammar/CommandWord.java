package org.kathrynhuxtable.acode.grammar;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.kathrynhuxtable.acode.grammar.tree.AcdNode;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommandWord {

	public enum CommandStatus { GOOD, AMBIGUOUS, NOT_FOUND }

	private String word;
	private List<AcdNode> nodes;
	private CommandStatus status;

	public AcdNode getNode() {
		return nodes.size() != 1 ? null : nodes.get(0);
	}
}
