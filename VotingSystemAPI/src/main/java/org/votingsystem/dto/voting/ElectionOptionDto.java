package org.votingsystem.dto.voting;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * License: https://github.com/votingsystem/votingsystem/wiki/Licencia
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ElectionOptionDto {

    private String content;
    private Long numVoteRequests;
    private Long numUsersWithVote;
    private Long numRepresentativesWithVote;
    private Long numVotesResult;
    
    public ElectionOptionDto() {}
    
    public ElectionOptionDto(String content, Long numVoteRequests, Long numUsersWithVote,
                             Long numRepresentativesWithVote, Long numVotesResult) {
        this.content = content;
        this.numVoteRequests = numVoteRequests;
        this.numUsersWithVote = numUsersWithVote;
        this.numRepresentativesWithVote = numRepresentativesWithVote;
        this.numVotesResult = numVotesResult;
    }


    public String getContent() {
        return content;
    }

    public Long getNumVoteRequests() {
        return numVoteRequests;
    }

    public Long getNumUsersWithVote() {
        return numUsersWithVote;
    }

    public Long getNumRepresentativesWithVote() {
        return numRepresentativesWithVote;
    }

    public Long getNumVotesResult() {
        return numVotesResult;
    }

    public void addNumVotesResult(Long votesAddded) {
        numVotesResult = numVotesResult + votesAddded;
    }

}
