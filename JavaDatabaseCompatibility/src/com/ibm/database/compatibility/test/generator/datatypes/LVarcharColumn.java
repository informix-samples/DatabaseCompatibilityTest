package com.ibm.database.compatibility.test.generator.datatypes;

import java.util.Arrays;
import java.util.List;

import com.ibm.database.compatibility.SqlDataType;

public class LVarcharColumn extends AbstractColumn implements Column {
	
	protected int colLength;

	protected static final List<String> lines = Arrays.asList(
		"Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. ",
		"Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. ",
		"Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. ",
		"Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum. ",
		"Lorem ipsum dolor sit amet, luptatum voluptatibus an eam, ne perpetua maluisset scriptorem duo. ",
		"Cu eam diam utroque, pro ex adipisci scripserit. ",
		"Eius propriae comprehensam et mea, tale illum in sea. ",
		"Ea vim molestiae elaboraret, aliquid blandit ea pri. ",
		"Id vix docendi explicari honestatis, noster albucius reformidans mei cu. ",
		"Has habeo ponderum ea. ",
		"Qui et fierent definitiones, vis altera albucius efficiendi ne. ",
		"Latine prodesset dissentiunt per no, eu vidit quando consetetur mei, veniam intellegat est at. ",
		"Usu at vitae consetetur efficiantur. ",
		"Et vis tamquam labores consulatu, te sea ridens option percipit, est te assum quando. ",
		"Quas persecuti scribentur vim ex, alia assum cu pro. ",
		"Ex praesent interpretaris eos, paulo pertinax eos id. ",
		"Rebum recteque referrentur ea his. ",
		"Te porro prodesset honestatis eam, ea vix ferri populo indoctum. ",
		"Vix ea rationibus sadipscing ullamcorper, vocibus argumentum pro ut. ",
		"Ex mei ceteros delectus, ad sea constituto complectitur, qui affert mnesarchum no. ",
		"Alii docendi mei eu, prima decore voluptatum duo ad. ",
		"Vix ne iuvaret aliquando quaerendum, inermis phaedrum iracundia ea vim. ", 
		"Repudiare assentior in eos. Sed ei dicat harum commodo, probo alienum delicata ex qui. ",
		"At enim labores pro, duo verterem euripidis ei. Ea erant legere adipisci pri, vix no aliquam scaevola consequat. ", 
		"Mea in vidisse scripserit. ",
		"Cibo iracundia reformidans duo in, debet aliquip deseruisse ad nam. ",
		"No malorum persius epicuri est, eos ex omnis primis periculis. ",
		"Eam impedit ponderum id, mazim virtute ut vix. ",
		"Duo ex dolorem singulis, ex eam mazim putent persecuti, ad dolore admodum nec.");
	protected final int nunique = lines.size();
	
	public LVarcharColumn(String colName, int colLength, int seed) {
		super(SqlDataType.LVARCHAR, colName, seed);
		this.colLength = colLength;
	}
	
	@Override
	public String getColumnTypeAsSQLString() {
		return super.getColumnTypeName() + "(" + colLength + ")";
	}
	
	public Object getValue(int i) {
		int index = i + seed;
		StringBuilder sb = new StringBuilder();
		while (sb.length() < colLength) {
			String next = lines.get(index % nunique);
			if (sb.length() + next.length() > colLength) {
				break;
			}
			sb.append(next);
			index++;
		}
		return sb.toString();
	}
	
	public Class<?> getJavaClassName() {
		return String.class;
	}

}
