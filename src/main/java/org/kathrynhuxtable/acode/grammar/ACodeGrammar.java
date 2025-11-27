package org.kathrynhuxtable.acode.grammar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ACodeGrammar {

	public static final int IDENTIFIER = 0;
	public static final int TEXTSTRING = 1;
	public static final int CODEFRAGMENT = 2;

	public static final int _UNK = 0;
	public static final int _OBJ = 1;
	public static final int _PLA = 2;
	public static final int _BST = 3;
	public static final int _NOD = 4;
	public static final int _DEM = 5;
	public static final int _FNC = 6;

	char[] lblChar = new char[] { 'u', 'o', 'p', 'b', 'n', 'd', 'f' };
	int[] lblIndex = new int[] { 0, 0, 0, 0, 0, 0, 0 };

	public int secflg;
	
	private int funcNum = 0;

	public List<String[]> plclst = new ArrayList<String[]>();

	public List<String> defaultDirections = new ArrayList<String>();

	private static ACodeGrammar instance;

	private Map<String, Element> identList = new HashMap<String, Element>();

	private ACodeGrammar() {
	}

	public static ACodeGrammar getInstance() {
		if (instance == null) {
			instance = new ACodeGrammar();
		}

		return instance;
	}

	public String genFunctionName() {
		return "func." + funcNum++;
	}

	public Element findIdent(String name) {
		Element ident = identList.get(name);
		if (ident == null) {
			ident = new Element();
			ident.name = name;
			ident.label = name;
			ident.value = _UNK;

			identList.put(name, ident);
		}

		return ident;
	}

	public int findType(String name) {
		if ("_UNK".equals(name)) {
			return _UNK;
		}
		if ("_OBJ".equals(name)) {
			return _OBJ;
		}
		if ("_PLA".equals(name)) {
			return _PLA;
		}
		if ("_BST".equals(name)) {
			return _BST;
		}
		if ("_NOD".equals(name)) {
			return _NOD;
		}
		if ("_DEM".equals(name)) {
			return _DEM;
		}
		if ("_FNC".equals(name)) {
			return _FNC;
		}
		return _UNK;
	}

	public void putFunction(Element fnc) {
		System.out.println(fnc.label + "( me, args )\t/*    */");
		String fncText = fnc.desc.get(0);
		System.out.println(fncText.substring(1, fncText.length() - 1));
	}

	public void putBeast(Element beast) {
		System.out.println(beast.label);
		System.out.println("{\n\t_BST,\n\tNIL,"); /* TYPE, NEXT */
		System.out.println("\t" + beast.desc.get(0) + ","); /* DESC */
		System.out.println("\t\"" + beast.name + "\","); /* NAME */
		System.out.println("\tNIL,\n\tNIL,"); /* PLACE, OBJLST */
		System.out.println("\t" + beast.power + ","); /* POWER */
		System.out.println("\tNIL,"); /* B.EVENT */
		System.out.println("\t" + beast.function.label + ","); /* B.FUNC */
		System.out.println("\tNIL,"); /* B.NXTBST */

		putArgs(beast.args);
		System.out.println("\tNIL,\n\tNIL,\n\tNIL,\n\tNIL,\n\tNIL"); /* ARGS */
		System.out.println("};\n\n");

		putList(beast.objects, beast);
	}

	public Element putNode(Element nxtnode, Element node) {
		System.out.println(node.label);

		System.out.println("{\n\t_NODE,\n\t" + (nxtnode == null ? null : nxtnode.label) + ","); /* TYPE, NEXT */
		System.out.println("\t{"); /* DESC */

		for (String d : node.desc) {
			System.out.println("\t\t" + d + ",");
		}

		System.out.println("\t\tNIL\n\t},");

		System.out.println("\t\"" + node.name + "\",\n"); /* NAME */

		System.out.println("\t{\n"); /* N.PATHS */

		for (Direction dir : node.direction) {
			if (dir.type == ':') {
				System.out.print("\t\t{\t\"" + dir.direction + "\",\tmove,");
				System.out.println("\t" + dir.destination.label + ",");
				if (dir.description == null) {
					System.out.println("\t\"\",");
				} else {
					putArgs(dir.description);
				}
				System.out.println("\tNIL\n\t\t},");
			} else {
				System.out.print("\t\t{\t\"" + dir.direction + "\",\t" + dir.destination.label + ",");
				putArgs(dir.description);
				System.out.println("\tNIL\n\t\t},");
			}
		}

		for (String dir : defaultDirections) {
			if (!findDirection(node.direction, dir)) {
				System.out.println("\t\t{\t\"" + dir + "\",\tmove,\tNIL,");
				System.out.println("\t\t\t\"\",\tNIL");
				System.out.println("\t\t},");
			}
		}

		System.out.println("\t\tNIL\n\t},\n");

		System.out.println("\tNIL,\n\tNIL,\n\tNIL,\n\tNIL\n"); /* OBJLST, N.PLALST, N.BSTLST, N.VISIT */

		System.out.println("};\n\n");

		putList(node.list, node);

		return node;
	}

	public void putObject(Element obj) {
		System.out.println(obj.label);
		System.out.println("{\n\t_OBJ,\n\tNIL,"); /* TYPE, NEXT */
		System.out.println("\t" + obj.desc.get(0) + ","); /* DESC */
		System.out.println("\t\"" + obj.name + "\","); /* NAME */
		System.out.println("\tNIL,\n\tNIL,"); /* PLACE, OBJLST */
		System.out.println("\t" + obj.power + ","); /* POWER */
		System.out.println("\t{"); /* O.VERB */

		for (Verb verb : obj.verb) {
			System.out.print("\t\t{\t\"" + verb.verb + "\",\t" + verb.function + ",");
			putArgs(verb.args);
			System.out.println("\tNIL\t},");
		}

		System.out.println("\t\tNIL\n\t},");

		putArgs(obj.args);
		System.out.println("\tNIL,\n\tNIL,\n\tNIL,\n\tNIL,\n\tNIL"); /* ARGS */
		System.out.println("};");
		System.out.println();
	}

	public void putPlayer(Element pla) {
		System.out.println(pla.label + "\n{\n\t_PLA,\n\tNIL,"); /* TYPE, NEXT */
		System.out.println("\t" + pla.desc.get(0) + ","); /* DESC */
		System.out.println("\t\"" + pla.name + "\","); /* NAME */

		System.out.println("\tNIL,\n\tNIL,"); /* PLACE, OBJLST */
		System.out.println("\t" + pla.power + ","); /* POWER */
		System.out.println("\tNIL,\n\tNIL,\n\tNIL"); /* P.MESG, P.TIME, P.NDEATHS */
		System.out.println("};");
		System.out.println();

		putList(pla.objects, pla);
	}

	private void putArgs(List<Arg> args) {
		if (args != null) {
			for (Arg arg : args) {
				if (arg.type == TEXTSTRING) {
					System.out.print("\t" + arg.value + ",");
				} else if (arg.type == IDENTIFIER) {
					System.out.print("\t" + ((Element) arg.value).label + ",");
				} else {
					String text = (String) arg.value;
					text = text.substring(1, text.length() - 1);
					System.out.print("\t" + text + ",");
				}
			} 
		}
	}

	private void putList(List<Element> list, Element place) {
		if (list != null) {
			for (Element element : list) {
				String[] e = new String[2];
				e[0] = element.label;
				e[1] = place.label;
				plclst.add(e);
			} 
		}
	}

	private boolean findDirection(List<Direction> list, String dir) {
		for (Direction d : list) {
			if (d.direction.equals(dir)) {
				return true;
			}
		}

		return false;
	}

	public Element defnid(Element p, int type) {
		if (p.value == type && p.fdef) {
			p.fdef = false;
			return p;
		} else if (p.value != _UNK) {
			return null;
		}

		p.value = type;
		p.fdef = false;

		p.label = String.format("%c.%04d", lblChar[type], lblIndex[type]++);

		return p;
	}

	public Element fdefid(Element p, int type) {
		if (p.value == type) {
			return p;
		} else if (p.value != _UNK) {
			return null;
		}

		p.value = type;
		p.fdef = true;

		p.label = String.format("%c.%04d", lblChar[type], lblIndex[type]++);

		return p;
	}

}
