package org.pushpmx;

import org.ecj.psh.PshIndividual;

import ec.EvolutionState;
import ec.Individual;
import ec.gp.koza.KozaFitness;
import ec.simple.SimpleShortStatistics;

/**
 * Subclass of SimpleShortStatics. The only difference is that we write standardized fitnesses to stat file.
 * 
 * @author Tomasz Kamiński
 * 
 */
public class PshShortStatistics extends SimpleShortStatistics {

	/**
	 * Prints out the statistics, but does not end with a println -- this lets
	 * overriding methods print additional statistics on the same line
	 */
	protected void _postEvaluationStatistics(final EvolutionState state) {
		// gather timings
		super._postEvaluationStatistics(state);

		// TODO check somehow whether Individuals have KozaFitness

		Individual[] best_i = new Individual[state.population.subpops.length];
		for (int x = 0; x < state.population.subpops.length; x++) {

			// standardized fitness information
			double meanStandardizedFitness = 0.0;

			// mean size of root stack
			double meanRootStackSize = 0.0f;

			// best individual root stack size
			int bestRootStackSize = 0;

			for (int y = 0; y < state.population.subpops[x].individuals.length; y++) {
				// best individual
				if (best_i[x] == null
						|| state.population.subpops[x].individuals[y].fitness
								.betterThan(best_i[x].fitness)) {
					best_i[x] = state.population.subpops[x].individuals[y];
					bestRootStackSize = ((PshIndividual) best_i[x]).program
							.size();
				}

				// mean fitness for population
				meanStandardizedFitness += ((KozaFitness) state.population.subpops[x].individuals[y].fitness)
						.standardizedFitness();

				meanRootStackSize += ((PshIndividual) state.population.subpops[x].individuals[y]).program
						.size();
			}

			// compute fitness stats
			meanStandardizedFitness /= state.population.subpops[x].individuals.length;

			// compute mean root stack size
			meanRootStackSize /= state.population.subpops[x].individuals.length;

			state.output.print(
					""
							+ meanStandardizedFitness
							+ " "
							+ ((KozaFitness) best_i[x].fitness)
									.standardizedFitness()
							+ " "
							+ ((KozaFitness) best_of_run[x].fitness)
									.standardizedFitness() + " "
							+ meanRootStackSize + " " + bestRootStackSize + " "
							+ ((PshIndividual) best_of_run[x]).program.size()
							+ " ", statisticslog);

		}
		// we're done!
	}

	/** Logs the best individual of the run. */
	public void finalStatistics(final EvolutionState state, final int result) {
		// for (int x = 0; x < state.population.subpops.length; x++) {
		// PshIndividual ind = (PshIndividual) best_of_run[x];
		// KozaFitness fitness = (KozaFitness) ind.fitness;
		// state.output.println(
		// "" + fitness.isIdealFitness() + " " + ind.size() + " "
		// + fitness.standardizedFitness() + " "
		// + fitness.adjustedFitness(), statisticslog);
		// }
	}

}
