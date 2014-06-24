/*******************************************************************************
 * Copyright 2014 University of Southern California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This code was developed by the Information Integration Group as part 
 * of the Karma project at the Information Sciences Institute of the 
 * University of Southern California.  For more information, publications, 
 * and related projects, please see: http://www.isi.edu/integration
 ******************************************************************************/
package edu.isi.karma.kr2rml.template;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.isi.karma.kr2rml.URIFormatter;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.Row;

public class TemplateTermSetPopulator {
	
	protected TemplateTermSet originalTerms;
	private boolean URIify;
	private boolean useNodeValue;
	private StringBuilder baseTemplate;
	private URIFormatter formatter;
	
	public TemplateTermSetPopulator(TemplateTermSet originalTerms, StringBuilder baseTemplate, URIFormatter formatter)
	{
		configure(originalTerms, baseTemplate, formatter);
		this.URIify = true;
		this.useNodeValue = true;
	}
	
	public TemplateTermSetPopulator(TemplateTermSet originalTerms, StringBuilder baseTemplate, URIFormatter formatter, boolean URIify, boolean useNodeValue)
	{
		configure(originalTerms, baseTemplate, formatter);
		this.URIify = URIify;
		this.useNodeValue = useNodeValue;
	}
	
	private void configure(TemplateTermSet originalTerms, StringBuilder baseTemplate, URIFormatter formatter)
	{
		this.originalTerms = originalTerms;
		this.baseTemplate = baseTemplate;
		this.formatter = formatter;
	}
	
	public TemplateTermSet getTerms()
	{
		return originalTerms;
	}
	public List<PopulatedTemplateTermSet> populate(Row topRow, TemplateTermSetPopulatorPlan plan)
	{
		List<PartiallyPopulatedTermSet> partials = plan.execute(topRow);
		
		return generatePopulatedTemplatesFromPartials(partials);
	}

	
	
	
	public List<PopulatedTemplateTermSet> generatePopulatedTemplatesFromPartials(List<PartiallyPopulatedTermSet> partials)
	{
		return generatePopulatedTemplates(partials, baseTemplate, originalTerms.getAllTerms());
	}
	protected List<PopulatedTemplateTermSet> generatePopulatedTemplates(List<PartiallyPopulatedTermSet> partials, StringBuilder output,
			List<TemplateTerm> terms) {
		List<PopulatedTemplateTermSet> templates = new LinkedList<PopulatedTemplateTermSet>();
		
		for(PartiallyPopulatedTermSet partial : partials)
		{
			StringBuilder uri = new StringBuilder();
			Map<ColumnTemplateTerm, Node> references = new HashMap<ColumnTemplateTerm, Node>();
			boolean termsSatisifed = true;
			boolean atLeastOneTermSatisified = false;
			for(TemplateTerm term : terms)
			{
				if(term instanceof ColumnTemplateTerm)
				{
					Node n = partial.getValue((ColumnTemplateTerm)term);
					if(n == null)
					{
						termsSatisifed = false; 
						continue;
					}
					if(n.getValue() == null || n.getValue().asString() == null || n.getValue().isEmptyValue() || n.getValue().asString().trim().isEmpty())
					{
						termsSatisifed = false;
					}
					else
					{
						atLeastOneTermSatisified = true;
					}
					references.put((ColumnTemplateTerm) term, n);
					if(useNodeValue)
					{
						uri.append(n.getValue().asString());
					}
					else
					{
						uri.append("_");
						uri.append(n.getId());
					}
				}
				else
				{
					uri.append(term.getTemplateTermValue());
				}
			}
			if(termsSatisifed || (!useNodeValue && atLeastOneTermSatisified))
			{
				String value = uri.toString();
				if(URIify)
				{
					value = formatter.getExpandedAndNormalizedUri(value);
				}
				templates.add(new PopulatedTemplateTermSet(originalTerms, references, value));
			}
			
		}
		return templates;
	}
	
}
