using AutoMapper;
using JNBFitness.Application.DTOs.Utilisateur;
using JNBFitness.Infrastructure.Repositories;

namespace JNBFitness.Application.Services.Utilisateur
{
    public class ClientService : IClientService
    {
        private readonly IClientRepository _clientRepository;
        private readonly IMapper _mapper;

        public ClientService(IClientRepository clientRepository, IMapper mapper)
        {
            _clientRepository = clientRepository;
            _mapper = mapper;
        }

        public async Task<ClientDto> GetByIdAsync(long utilisateurId)
        {
            var client = await _clientRepository.GetByUtilisateurIdAsync(utilisateurId);
            if (client == null)
                throw new Exception("Client introuvable");

            return _mapper.Map<ClientDto>(client);
        }

        public async Task<IEnumerable<ClientDto>> GetAllClientsAsync()
        {
            var clients = await _clientRepository.GetClientsActifsAsync();
            return _mapper.Map<IEnumerable<ClientDto>>(clients);
        }

        public async Task<ClientDto> GetByCodeParrainageAsync(string codeParrainage)
        {
            var client = await _clientRepository.GetByCodeParrainageAsync(codeParrainage);
            if (client == null)
                throw new Exception("Client introuvable avec ce code de parrainage");

            return _mapper.Map<ClientDto>(client);
        }
    }
}
