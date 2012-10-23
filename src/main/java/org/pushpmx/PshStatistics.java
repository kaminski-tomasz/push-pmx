/*
* Copyright 2012 Tomasz Kamiński
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.pushpmx;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.ecj.psh.PshEvaluator;
import org.ecj.psh.PshEvolutionState;
import org.ecj.psh.PshIndividual;
import org.ecj.psh.util.Simplifier;
import org.pushpmx.problem.FloatSymbolicRegression;
import org.spiderland.Psh.Interpreter;
import org.spiderland.Psh.Program;
import org.spiderland.Psh.SemanticInterpreter;

import ec.EvolutionState;
import ec.Individual;
import ec.Subpopulation;
import ec.gp.koza.KozaFitness;
import ec.simple.SimpleShortStatistics;
import ec.util.MersenneTwisterFast;
import ec.util.Parameter;

/**
 * Subclass of SimpleShortStatics.
 * 
 * @author Tomasz Kamiński
 * 
 */
public class PshStatistics extends SimpleShortStatistics {
	public static final String P_INSTRUCTIONLIST = "instruction-list";

    /** log file parameter */
    public static final String P_SUMMARY_FILE = "summary-file";
	
    /** The summary Statistics' log */
    public int summarylog;
    
    /** The number of taken steps*/
    public long lastTotalStepsTaken;    
    
    /** Total number of individuals in all subpopulations */
    public long totalPopulationSize;
    
    public long rootStackLengths[];

	private KozaFitness[] best_of_run_fitness_test;

	private int[] best_of_run_gen;
	
	private long totalTimeTaken;    
	
	@Override
	public void setup(final EvolutionState state, final Parameter base) {
		super.setup(state, base);
		File summaryFile = state.parameters.getFile(base.push(P_SUMMARY_FILE),
				null);

		if (summaryFile != null)
			try {
				summarylog = state.output.addLog(summaryFile,
						!state.parameters.getBoolean(base.push(P_COMPRESS),
								null, false), state.parameters.getBoolean(
								base.push(P_COMPRESS), null, false));
			} catch (IOException i) {
				state.output
						.fatal("An IOException occurred while trying to create the log "
								+ summaryFile + ":\n" + i);
			}		
	}
	
	@Override
	public void preInitializationStatistics(final EvolutionState state) {
		super.preInitializationStatistics(state);
		if (doFull) {
			lastTotalStepsTaken = getTotalStepsTaken(state);
			totalTimeTaken = System.currentTimeMillis();
		}
	}
	
	@Override
	public void postInitializationStatistics(final EvolutionState state) {
		super.postInitializationStatistics(state);
		
		// set up arrays
		best_of_run_fitness_test = new KozaFitness[state.population.subpops.length];
		best_of_run_gen = new int[state.population.subpops.length];
		
		// compute total population size
		totalPopulationSize = 0;
		for (Subpopulation subpop : state.population.subpops) {
			totalPopulationSize += subpop.individuals.length;
		}
		
		if (doFull) {
            rootStackLengths = new long[state.population.subpops.length];
            for(int x=0;x<rootStackLengths.length;x++) rootStackLengths[x] = 0;
			state.output.print(""
					+ (getTotalStepsTaken(state) - lastTotalStepsTaken) + " ",
					statisticslog);
		}
	}
	
	@Override
	public void preBreedingStatistics(final EvolutionState state) {
		super.preBreedingStatistics(state);
		if (doFull) {
			lastTotalStepsTaken = getTotalStepsTaken(state);
		}
	}

	@Override
	public void postBreedingStatistics(final EvolutionState state) {
		super.postBreedingStatistics(state);
		if (doFull) {
			state.output.print(""
					+ (getTotalStepsTaken(state) - lastTotalStepsTaken) + " ",
					statisticslog);
		}
	}
	
	@Override
	public void preEvaluationStatistics(final EvolutionState state) {
		super.preEvaluationStatistics(state);
		if (doFull) {
			lastTotalStepsTaken = getTotalStepsTaken(state);
		}
	}
	
	/**
	 * Prints out the statistics, but does not end with a println -- this lets
	 * overriding methods print additional statistics on the same line
	 */
	protected void _postEvaluationStatistics(final EvolutionState state) {
		// gather timings
		if (doFull) {
			Runtime r = Runtime.getRuntime();
			long curU = r.totalMemory() - r.freeMemory();
			state.output.print("" + (System.currentTimeMillis() - lastTime)
					+ " ", statisticslog);
			state.output.print("" + (curU - lastUsage) + " ", statisticslog);
			state.output.print(""
					+ (getTotalStepsTaken(state) - lastTotalStepsTaken) + " ",
					statisticslog);
		}	

		// mean steps taken for individual so far
		double meanStepsTakenSoFar = ((double) getTotalStepsTaken(state))
				/ (totalPopulationSize * (state.generation + 1));
		
		state.output.print("" + meanStepsTakenSoFar + " ", statisticslog);
		
		// total execution evaluations so far
		state.output.print("" + getEvaluationExecutions(state) + " ", statisticslog);
		
		long lengthPerGen = 0, rootStackLengthPerGen = 0;
		Individual[] best_i = new Individual[state.population.subpops.length];
		for (int x = 0; x < state.population.subpops.length; x++) {
			if (doFull) {
				lengthPerGen = 0;
				rootStackLengthPerGen = 0;
				for (int y = 0; y < state.population.subpops[x].individuals.length; y++) {
					long size = state.population.subpops[x].individuals[y]
							.size(); 
					lengthPerGen += size;
					lengths[x] += size;

					long rootStackLength = ((PshIndividual)state.population.subpops[x].individuals[y])
							.program.size();
					rootStackLengthPerGen += rootStackLength;
					rootStackLengths[x] += rootStackLength;
					
				}

				state.output.print("" + ((double) lengthPerGen)
						/ state.population.subpops[x].individuals.length + " ",
						statisticslog);

				state.output
						.print(""
								+ ((double) lengths[x])
								/ (state.population.subpops[x].individuals.length * (state.generation + 1))
								+ " ", statisticslog);
				
				state.output.print("" + ((double) rootStackLengthPerGen)
						/ state.population.subpops[x].individuals.length + " ",
						statisticslog);

				state.output
						.print(""
								+ ((double) rootStackLengths[x])
								/ (state.population.subpops[x].individuals.length * (state.generation + 1))
								+ " ", statisticslog);
			}

			// fitness information
			double meanFitness = 0.0;

			for (int y = 0; y < state.population.subpops[x].individuals.length; y++) {
				// best individual
				if (best_i[x] == null
						|| state.population.subpops[x].individuals[y].fitness
								.betterThan(best_i[x].fitness))
					best_i[x] = state.population.subpops[x].individuals[y];

				// mean fitness for population
				meanFitness += ((KozaFitness)state.population.subpops[x].individuals[y].fitness)
						.standardizedFitness();
			}

			// compute fitness stats
			meanFitness /= state.population.subpops[x].individuals.length;
			state.output.print("" + meanFitness + " ", statisticslog);
			
			// best of generation fitness
			state.output.print("" + ((KozaFitness) best_i[x].fitness)
									.standardizedFitness() + " ", statisticslog);

			// best of generation fitness computed for testing set
			KozaFitness testSetFitness = computeFitnessForTestingSet(state, best_i[x]);
			state.output.print("" + (testSetFitness).standardizedFitness()
					+ " ", statisticslog);
			
			// now test to see if it's the new best_of_run[x]
			if (best_of_run[x] == null
					|| best_i[x].fitness.betterThan(best_of_run[x].fitness)) {
				best_of_run[x] = (Individual) (best_i[x].clone());
				// save the fitness for testing set and the number of generation
				best_of_run_fitness_test[x] = (KozaFitness)(testSetFitness.clone());
				best_of_run_gen[x] = state.generation;
			}

			state.output.print("" + best_of_run_gen[x] + " ", statisticslog);
			state.output.print("" + ((KozaFitness)best_of_run[x].fitness).standardizedFitness() + " ",
					statisticslog);
			state.output.print("" + (best_of_run_fitness_test[x]).standardizedFitness() + " ",
					statisticslog);

			if (doFull) {
				state.output
						.print("" + (double) (best_i[x].size()) + " "
								+ (double) (best_of_run[x].size()) + " "
								+ (double) (((PshIndividual)best_i[x]).program.size()) + " "
								+ (double) (((PshIndividual)best_of_run[x]).program.size()) + " ",
								statisticslog);
			}
		}
		// we're done!
	}
	
	private KozaFitness computeFitnessForTestingSet(EvolutionState state,
			Individual individual) {
		PshIndividual ind = (PshIndividual) individual.clone();
		FloatSymbolicRegression problem = ((FloatSymbolicRegression) (state.evaluator.p_problem
				.clone()));
		Interpreter interpreter = ((PshEvolutionState)state).interpreter[0];
		problem.evaluateTestSet(state, 0, interpreter, ind);
		return (KozaFitness) (ind.fitness);
	}

	private long getEvaluationExecutions(final EvolutionState state) {
		long sum = 0;
		Interpreter[] interpreter = ((PshEvolutionState) state).interpreter;
		for (int i = 0; i < interpreter.length; i++) {
			sum += interpreter[i].GetEvaluationExecutions();
		}
		return sum;
	}

	private long getTotalStepsTaken(final EvolutionState state) {
		long sum = 0;
		Interpreter[] interpreter = ((PshEvolutionState) state).interpreter;
		for (int i = 0; i < interpreter.length; i++) {
			sum += interpreter[i].getTotalStepsTaken();
		}
		return sum;
	}

	/** Logs the best individual of the run. */
	public void finalStatistics(final EvolutionState state, final int result) {
		FloatSymbolicRegression problem = ((FloatSymbolicRegression) (state.evaluator.p_problem
				.clone()));
		
		int steps = 10000;
		float simplifyByFlattenProb = 0.2f;
		
		state.output.print(""+ ((result == EvolutionState.R_SUCCESS) ? 1 : 0) + 
				" " + (System.currentTimeMillis() - totalTimeTaken) + 
				" " + getTotalStepsTaken(state) + " " , summarylog);


		// mean steps taken for individual so far
		double meanStepsTakenSoFar = ((double) getTotalStepsTaken(state))
				/ (totalPopulationSize * (state.generation + 1));
		
		state.output.print("" + meanStepsTakenSoFar + " ", summarylog);
		
		state.output.print("" + getEvaluationExecutions(state) + " ",
				summarylog);

		state.output.println("" + (problem.numOfTrainPoints) + " ", summarylog);
		
		
		for (int x = 0; x < state.population.subpops.length; x++) {
			state.output.print("" + (best_of_run_gen[x]) + " ", summarylog);
			
			state.output.print("" + (((((KozaFitness) best_of_run[x].fitness)
					.standardizedFitness()) <= ((PshEvaluator)state.evaluator).idealThreshold)?1:0)  + " ", summarylog);
			
			state.output.print("" + (((KozaFitness) best_of_run[x].fitness)
									.standardizedFitness()) + " ", summarylog);

			state.output.print(""
					+ (((KozaFitness) best_of_run[x].fitness).hits) + " ",
					summarylog);
			
			state.output.print("" + ((((best_of_run_fitness_test[x])
					.standardizedFitness()) <= ((PshEvaluator)state.evaluator).idealThreshold)?1:0)  + " ", summarylog);
			
			state.output.print("" + (best_of_run_fitness_test[x]).
									standardizedFitness() + " ", summarylog);

			state.output.print("" + (best_of_run_fitness_test[x]).hits + " ",
					summarylog);
			
			state.output.print("" + (double)((PshIndividual) best_of_run[x]).size()
					+ " ", summarylog);
			state.output.println(
					"" + (double)((PshIndividual) best_of_run[x]).program.size() + " ",
					summarylog);

			state.output.println("" + ((PshIndividual) best_of_run[x]).program
					+ " ", summarylog);

			PshIndividual simpler = (PshIndividual)best_of_run[x].clone();
			Simplifier.autoSimplify(state, simpler, problem, steps, x, 0, simplifyByFlattenProb);
			
			state.output.println("" + simpler.program + " ", summarylog);
			
			problem.describe(state, (PshIndividual)best_of_run[x], x, 0, summarylog);
			problem.describe(state, simpler, x, 0, summarylog);
		}
	}

}
