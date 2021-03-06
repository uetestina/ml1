package org.totalboumboum.ai.v201415.ais._example.v0.criterion;

import org.totalboumboum.ai.v201415.adapter.agent.AiCriterionBoolean;
import org.totalboumboum.ai.v201415.adapter.data.AiTile;
import org.totalboumboum.ai.v201415.ais._example.v0.Agent;

/**
 * Cette classe est un simple exemple de 
 * critère binaire. Copiez-la, renommez-la, modifiez-la
 * pour l'adapter à vos besoin.
 * 
 * @author Xxxxxx
 * @author Yyyyyy
 * @author Zzzzzz
 */
public class CriterionFirst extends AiCriterionBoolean<Agent>
{	/** Nom de ce critère */
	public static final String NAME = "FIRST_CRITERION";
	
	/**
	 * Crée un nouveau critère binaire.
	 * 
	 * @param ai
	 * 		l'agent concerné. 
	 */
	public CriterionFirst(Agent ai)
	{	super(ai,NAME);
		ai.checkInterruption();
	}

	/////////////////////////////////////////////////////////////////
	// PROCESS					/////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	protected Boolean processValue(AiTile tile)
	{	ai.checkInterruption();
		boolean result = true;
		
		/*
		 *  TODO à compléter par le traitement approprié.
		 *  
		 *  Remarque : ce commentaire est à effacer, comme tous les autres marqueurs TODO
		 */
	
		return result;
	}
}
